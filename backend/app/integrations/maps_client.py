import os
import logging
from typing import Dict, Any, List, Optional, Tuple
import httpx

from app.utils.geo_utils import calculate_haversine_distance

logger = logging.getLogger(__name__)

# Standard headers for OpenStreetMap API compliance (requires a custom User-Agent)
HEADERS = {
    "User-Agent": "NavAssistApp/1.0 (contact: support@navassist.in)"
}

# Backward compatibility placeholder
gmaps_client = None


async def get_route_details(
    pickup_lat: float, 
    pickup_lon: float, 
    dest_lat: float, 
    dest_lon: float
) -> Dict[str, Any]:
    """
    Computes distance (meters), duration (seconds), and suggested fare (INR) for a booking route.
    Utilizes OSRM (Open Source Routing Machine) public routing API.
    """
    url = f"https://router.project-osrm.org/route/v1/driving/{pickup_lon},{pickup_lat};{dest_lon},{dest_lat}?overview=false"
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(url, headers=HEADERS)
            if response.status_code == 200:
                data = response.json()
                routes = data.get("routes", [])
                if routes:
                    distance_meters = routes[0]["distance"]
                    duration_seconds = routes[0]["duration"]
                    
                    # Fare estimation: Rs 50 base fee + Rs 15 per kilometer
                    distance_km = distance_meters / 1000.0
                    estimated_fare = round(max(50.0, 50.0 + (distance_km * 15.0)))
                    
                    return {
                        "distance_meters": int(distance_meters),
                        "duration_seconds": int(duration_seconds),
                        "estimated_fare": estimated_fare,
                        "provider": "osrm"
                    }
    except Exception as e:
        logger.error(f"OSRM routing API error: {e}. Falling back to Haversine.")
        
    # Haversine straight-line distance fallback
    distance_km = calculate_haversine_distance(pickup_lat, pickup_lon, dest_lat, dest_lon)
    estimated_road_distance_km = distance_km * 1.3
    distance_meters = int(estimated_road_distance_km * 1000)
    
    # Calculate duration assuming an average city speed of 25 km/h
    average_speed_mps = 6.94
    duration_seconds = int(distance_meters / average_speed_mps)
    
    # Suggest fare: Rs 50 base + Rs 15 per kilometer
    estimated_fare = round(max(50.0, 50.0 + (estimated_road_distance_km * 15.0)))
    
    return {
        "distance_meters": distance_meters,
        "duration_seconds": duration_seconds,
        "estimated_fare": estimated_fare,
        "provider": "haversine_fallback"
    }


async def geocode_address(address: str) -> Dict[str, Any]:
    """
    Translates a text address into physical geographic (latitude, longitude) coordinates.
    Uses Nominatim OpenStreetMap API.
    """
    url = f"https://nominatim.openstreetmap.org/search?q={address}&format=json&limit=1"
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(url, headers=HEADERS)
            if response.status_code == 200:
                data = response.json()
                if data:
                    res = data[0]
                    return {
                        "latitude": float(res["lat"]),
                        "longitude": float(res["lon"]),
                        "formatted_address": res["display_name"]
                    }
    except Exception as e:
        logger.error(f"OSM Nominatim geocoding error: {e}")
        
    # Local mock fallback coordinate (e.g. New Delhi)
    return {
        "latitude": 28.6139,
        "longitude": 77.2090,
        "formatted_address": f"{address} (Mocked Coordinates)"
    }


async def autocomplete_place(input_text: str) -> List[Dict[str, Any]]:
    """
    Returns place search suggestions from Photon OSM API.
    """
    url = f"https://photon.komoot.io/api/?q={input_text}&limit=5"
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(url, headers=HEADERS)
            if response.status_code == 200:
                data = response.json()
                features = data.get("features", [])
                results = []
                for idx, feat in enumerate(features):
                    props = feat.get("properties", {})
                    name = props.get("name", "")
                    city = props.get("city", "")
                    state = props.get("state", "")
                    country = props.get("country", "")
                    
                    geom = feat.get("geometry", {})
                    coords = geom.get("coordinates", [72.8777, 19.0760]) # [lon, lat]
                    parts = [name, city, state, country]
                    desc = ", ".join([p for p in parts if p])
                    results.append({
                        "description": desc,
                        "place_id": str(props.get("osm_id", f"osm_{idx}")),
                        "latitude": float(coords[1]),
                        "longitude": float(coords[0])
                    })
                return results
    except Exception as e:
        logger.error(f"Photon place autocomplete error: {e}")
        
    return [{"description": f"{input_text} Station", "place_id": "mock_place_station_1", "latitude": 28.6139, "longitude": 77.2090}]


async def reverse_geocode(latitude: float, longitude: float) -> Dict[str, str]:
    """
    Converts coordinates back into a human-readable physical address.
    Uses Nominatim OpenStreetMap API.
    """
    url = f"https://nominatim.openstreetmap.org/reverse?lat={latitude}&lon={longitude}&format=json"
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(url, headers=HEADERS)
            if response.status_code == 200:
                data = response.json()
                return {"formatted_address": data.get("display_name", f"Location near ({latitude}, {longitude})")}
    except Exception as e:
        logger.error(f"OSM Nominatim reverse geocoding error: {e}")
        
    return {"formatted_address": f"Mock address near ({latitude:.4f}, {longitude:.4f})"}
