from slowapi import Limiter
from slowapi.util import get_remote_address

# Centralized Limiter instance using client IP addresses
limiter = Limiter(key_func=get_remote_address)
