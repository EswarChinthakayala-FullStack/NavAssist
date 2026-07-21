import os
import uuid
import logging
from typing import Optional
from app.core.config import settings

logger = logging.getLogger(__name__)

# Disable S3 integration completely as per project specifications to run purely locally
s3_client = None

# Local directory configuration for uploads
LOCAL_STORAGE_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "uploads")
os.makedirs(LOCAL_STORAGE_DIR, exist_ok=True)


async def upload_file(file_content: bytes, original_filename: str, folder: str = "kyc") -> str:
    """
    Uploads a file payload locally to the uploads directory.
    Returns a unique identifier key/path string representing the stored object.
    """
    ext = os.path.splitext(original_filename)[1]
    unique_filename = f"{folder}/{uuid.uuid4()}{ext}"
    
    local_path = os.path.join(LOCAL_STORAGE_DIR, unique_filename.replace("/", "_"))
    os.makedirs(os.path.dirname(local_path), exist_ok=True)
    with open(local_path, "wb") as f:
        f.write(file_content)
        
    logger.info(f"File stored locally: {local_path}")
    return unique_filename


def generate_presigned_url(file_key: str, expiration: int = 900) -> str:
    """
    Returns the static URL path for a locally uploaded file.
    """
    safe_filename = file_key.replace("/", "_")
    return f"/static/{safe_filename}"


async def delete_file(file_key: str) -> bool:
    """
    Deletes a locally uploaded file.
    """
    local_path = os.path.join(LOCAL_STORAGE_DIR, file_key.replace("/", "_"))
    if os.path.exists(local_path):
        try:
            os.remove(local_path)
            logger.info(f"Deleted local file: {local_path}")
            return True
        except Exception as e:
            logger.error(f"Failed to delete local file {local_path}: {e}")
    return False
