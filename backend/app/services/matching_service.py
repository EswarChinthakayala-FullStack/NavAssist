import logging
import math
from typing import List, Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.assistant_repository import assistant_repository
from app.models.assistant import AssistantProfile

logger = logging.getLogger(__name__)


class MatchingService:
    @staticmethod
    async def find_nearby_assistants(
        db: AsyncSession,
        latitude: float,
        longitude: float,
        radius_km: float = 10.0
    ) -> List[Dict[str, Any]]:
        """
        Discovers online, KYC-verified assistants within a spatial radius,
        ranking them by a weighted combination of distance, trust score, and average rating.
        
        Rank Formula: (Trust Score * 0.4) + (Rating * 0.4) - (Distance KM * 0.2)
        """
        # Fetch from spatial DB coordinates
        assistants = await assistant_repository.get_nearby_assistants(db, latitude, longitude, radius_km)
        
        ranked_list = []
        for assistant in assistants:
            # Compute physical haversine spherical distance
            lat = assistant.current_latitude or 0.0
            lon = assistant.current_longitude or 0.0
            
            dlat = math.radians(lat - latitude)
            dlon = math.radians(lon - longitude)
            a = (math.sin(dlat / 2) ** 2 +
                 math.cos(math.radians(latitude)) * math.cos(math.radians(lat)) *
                 math.sin(dlon / 2) ** 2)
            c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
            distance_km = 6371 * c
            
            # Weighted normalization parameters
            trust_score = float(assistant.trust_score)
            rating = float(assistant.avg_rating)
            
            # Closer distances yield positive ranking offset subtraction
            rank_score = (trust_score * 0.4) + (rating * 0.4) - (distance_km * 0.2)
            
            ranked_list.append({
                "assistant_id": assistant.user_id,
                "assistant": assistant,
                "distance_km": distance_km,
                "rank_score": rank_score
            })
            
        # Sort descending by rank score
        ranked_list.sort(key=lambda x: x["rank_score"], reverse=True)
        return ranked_list
