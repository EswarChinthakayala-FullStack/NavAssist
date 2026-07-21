import enum
from datetime import datetime, timezone
from typing import Optional, Any
from sqlalchemy import String, ForeignKey, DateTime, Integer, Enum, Boolean, Index, Numeric
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base
from app.models.user import MySQLPoint, parse_wkb_point


class LocationLabel(str, enum.Enum):
    # Standard labels
    HOME = "home"
    OFFICE = "office"
    FAVORITE = "favorite"
    OTHER = "other"





class ServicePointType(str, enum.Enum):
    RAILWAY_STATION = "railway_station"
    AIRPORT = "airport"
    BUS_STAND = "bus_stand"
    GENERAL = "general"


class SavedLocation(Base):
    __tablename__ = "saved_locations"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    label: Mapped[LocationLabel] = mapped_column(Enum(LocationLabel), default=LocationLabel.OTHER, nullable=False)
    custom_label: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    address: Mapped[str] = mapped_column(String(500), nullable=False)
    coordinates: Mapped[Any] = mapped_column(MySQLPoint, nullable=False)
    place_id: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    @property
    def latitude(self) -> float:
        if self.coordinates and isinstance(self.coordinates, bytes):
            try:
                _, lat = parse_wkb_point(self.coordinates)
                return lat
            except Exception:
                pass
        return getattr(self, "_lat", 0.0)

    @latitude.setter
    def latitude(self, val: float):
        self._lat = val
        self._update_coordinates()

    @property
    def longitude(self) -> float:
        if self.coordinates and isinstance(self.coordinates, bytes):
            try:
                lon, _ = parse_wkb_point(self.coordinates)
                return lon
            except Exception:
                pass
        return getattr(self, "_lon", 0.0)

    @longitude.setter
    def longitude(self, val: float):
        self._lon = val
        self._update_coordinates()

    def _update_coordinates(self):
        lat = getattr(self, "_lat", None)
        lon = getattr(self, "_lon", None)
        if lat is not None and lon is not None:
            from sqlalchemy import func
            self.coordinates = func.ST_PointFromText(f"POINT({lon} {lat})", 4326)

    __table_args__ = (
        Index("idx_saved_coords", "coordinates", mysql_prefix="SPATIAL"),
    )


class ServicePoint(Base):
    __tablename__ = "service_points"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(150), nullable=False)
    type: Mapped[ServicePointType] = mapped_column(Enum(ServicePointType), nullable=False)
    city: Mapped[str] = mapped_column(String(100), nullable=False)
    state: Mapped[str] = mapped_column(String(100), nullable=False)
    code: Mapped[Optional[str]] = mapped_column(String(20), nullable=True)
    coordinates: Mapped[Any] = mapped_column(MySQLPoint, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=lambda: datetime.now(timezone.utc), 
        nullable=False
    )

    @property
    def latitude(self) -> float:
        if self.coordinates and isinstance(self.coordinates, bytes):
            try:
                _, lat = parse_wkb_point(self.coordinates)
                return lat
            except Exception:
                pass
        return getattr(self, "_lat", 0.0)

    @latitude.setter
    def latitude(self, val: float):
        self._lat = val
        self._update_coordinates()

    @property
    def longitude(self) -> float:
        if self.coordinates and isinstance(self.coordinates, bytes):
            try:
                lon, _ = parse_wkb_point(self.coordinates)
                return lon
            except Exception:
                pass
        return getattr(self, "_lon", 0.0)

    @longitude.setter
    def longitude(self, val: float):
        self._lon = val
        self._update_coordinates()

    def _update_coordinates(self):
        lat = getattr(self, "_lat", None)
        lon = getattr(self, "_lon", None)
        if lat is not None and lon is not None:
            from sqlalchemy import func
            self.coordinates = func.ST_PointFromText(f"POINT({lon} {lat})", 4326)

    __table_args__ = (
        Index("idx_service_point_coords", "coordinates", mysql_prefix="SPATIAL"),
    )
