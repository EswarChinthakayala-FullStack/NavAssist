import asyncio
import json
import logging
from typing import Dict, List
from fastapi import WebSocket

from app.core.redis_client import redis_client

logger = logging.getLogger(__name__)


class ConnectionManager:
    def __init__(self):
        # Maps channel names (e.g., "booking:tracking:12") to lists of active WebSockets
        self.active_subscriptions: Dict[str, List[WebSocket]] = {}
        # Maps channel names to their running asyncio listener task
        self.pubsub_tasks: Dict[str, asyncio.Task] = {}

    async def connect(self, websocket: WebSocket, channel: str):
        """Accepts a WebSocket connection and registers it to a specific subscription channel."""
        await websocket.accept()
        
        if channel not in self.active_subscriptions:
            self.active_subscriptions[channel] = []
            
        self.active_subscriptions[channel].append(websocket)
        logger.info(f"WebSocket client connected to channel: {channel}. Total subscriptions: {len(self.active_subscriptions[channel])}")
        
        # Start listening to Redis Pub/Sub for this channel if not already listening
        if channel not in self.pubsub_tasks:
            task = asyncio.create_task(self._redis_pubsub_listener(channel))
            self.pubsub_tasks[channel] = task

    async def disconnect(self, websocket: WebSocket, channel: str):
        """Removes a WebSocket connection from a subscription channel."""
        if channel in self.active_subscriptions:
            if websocket in self.active_subscriptions[channel]:
                self.active_subscriptions[channel].remove(websocket)
                
            # If no more active connections on this channel, clean up tasks & memory
            if not self.active_subscriptions[channel]:
                del self.active_subscriptions[channel]
                
                # Cancel Redis Pub/Sub task
                if channel in self.pubsub_tasks:
                    self.pubsub_tasks[channel].cancel()
                    del self.pubsub_tasks[channel]
                    
        logger.info(f"WebSocket client disconnected from channel: {channel}")

    async def broadcast_to_channel(self, channel: str, message: dict):
        """Sends a JSON message to all WebSockets directly connected to this worker instance on a channel."""
        if channel in self.active_subscriptions:
            payload = json.dumps(message)
            # Create a list copy to prevent race conditions during iteration
            for connection in list(self.active_subscriptions[channel]):
                try:
                    await connection.send_text(payload)
                except Exception as e:
                    logger.warning(f"Error sending socket message, client likely stale: {e}")
                    # Auto clean up failed connections
                    await self.disconnect(connection, channel)

    async def _redis_pubsub_listener(self, channel: str):
        """
        Runs in the background, listening to Redis Pub/Sub channels.
        Forwards incoming Redis payloads to all local WebSockets subscribed to this channel.
        """
        pubsub = redis_client.pubsub()
        await pubsub.subscribe(channel)
        logger.info(f"Subscribed worker to Redis Pub/Sub channel: {channel}")
        
        try:
            async for message in pubsub.listen():
                if message and message.get("type") == "message":
                    data = message.get("data")
                    if isinstance(data, str):
                        try:
                            parsed_data = json.loads(data)
                            await self.broadcast_to_channel(channel, parsed_data)
                        except json.JSONDecodeError:
                            await self.broadcast_to_channel(channel, {"raw": data})
        except asyncio.CancelledError:
            logger.info(f"Redis Pub/Sub listener task cancelled for channel: {channel}")
        except Exception as e:
            logger.error(f"Error in Redis Pub/Sub listener task for channel {channel}: {e}")
        finally:
            try:
                await pubsub.unsubscribe(channel)
                await pubsub.close()
            except Exception:
                pass


manager = ConnectionManager()
