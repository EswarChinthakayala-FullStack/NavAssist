import logging
from typing import Dict, Any, List
from app.integrations import maps

logger = logging.getLogger(__name__)


class GeoService:
    @staticmethod
    async def get_route(
        pickup_lat: float, 
        pickup_lon: float, 
        dest_lat: float, 
        dest_lon: float
    ) -> Dict[str, Any]:
        """
        Retrieves route metadata (road distance and duration) between pickup and destination.
        """
        return await maps.get_route_details(pickup_lat, pickup_lon, dest_lat, dest_lon)

    @staticmethod
    async def geocode_address(address: str) -> Dict[str, Any]:
        """
        Translates a text address into physical geographic (latitude, longitude) coordinates.
        """
        from app.integrations.maps_client import geocode_address as geocode
        return await geocode(address)

    @staticmethod
    async def autocomplete_place(input_text: str) -> List[Dict[str, Any]]:
        """
        Returns place search suggestion completions for typeahead fields.
        """
        from app.integrations.maps_client import autocomplete_place as autocomplete
        return await autocomplete(input_text)

    @staticmethod
    async def reverse_geocode(latitude: float, longitude: float) -> Dict[str, str]:
        """
        Converts latitude and longitude coordinates back into a human-readable physical address.
        """
        from app.integrations.maps_client import reverse_geocode as rev_geocode
        return await rev_geocode(latitude, longitude)
