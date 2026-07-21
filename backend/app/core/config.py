import os
from typing import Any, Dict, Optional
from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="ignore"
    )

    PROJECT_NAME: str = "NavAssist API"
    API_V1_STR: str = "/api/v1"
    SECRET_KEY: str = "dev-secret-key-change-in-production-navassist"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    # MySQL Configurations
    MYSQL_SERVER: str = "127.0.0.1"
    MYSQL_PORT: int = 3306
    MYSQL_USER: str = "root"
    MYSQL_PASSWORD: str = ""
    MYSQL_DB: str = "navassist"

    @property
    def SQLALCHEMY_DATABASE_URI(self) -> str:
        # FastAPI backend connects to MySQL using the async aiomysql driver
        password_part = f":{self.MYSQL_PASSWORD}" if self.MYSQL_PASSWORD else ""
        return f"mysql+aiomysql://{self.MYSQL_USER}{password_part}@{self.MYSQL_SERVER}:{self.MYSQL_PORT}/{self.MYSQL_DB}"

    # Redis Configurations
    REDIS_HOST: str = "127.0.0.1"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0

    @property
    def REDIS_URL(self) -> str:
        return f"redis://{self.REDIS_HOST}:{self.REDIS_PORT}/{self.REDIS_DB}"

    # Third Party Credentials / Integrations
    # Twilio/SMS Configs
    TWILIO_ACCOUNT_SID: str = ""
    TWILIO_AUTH_TOKEN: str = ""
    TWILIO_FROM_NUMBER: str = ""

    # SMTP Email Configs
    SMTP_HOST: str = "smtp.gmail.com"
    SMTP_PORT: int = 587
    SMTP_USER: str = "app.services.v1@gmail.com"
    SMTP_PASSWORD: str = "ubzx xugw lowv bgcv"
    SMTP_FROM: str = "app.services.v1@gmail.com"



    # Razorpay
    RAZORPAY_KEY_ID: str = "rzp_test_mockkeyid"
    RAZORPAY_KEY_SECRET: str = "mocksecret"
    RAZORPAY_WEBHOOK_SECRET: str = "mockwebhooksecret"


settings = Settings()
