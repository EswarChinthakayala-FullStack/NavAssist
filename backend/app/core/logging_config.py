import logging
import json
from datetime import datetime
from app.utils.timezone import to_ist

class JSONFormatter(logging.Formatter):
    """Custom log formatter that formats log records into a single-line JSON string."""
    def format(self, record: logging.LogRecord) -> str:
        # Construct log JSON payload
        log_data = {
            "timestamp": to_ist(datetime.fromtimestamp(record.created)).isoformat(),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
            "module": record.module,
            "funcName": record.funcName,
            "lineNo": record.lineno,
        }
        
        # Include context/extra attributes if any
        if hasattr(record, "extra") and isinstance(record.extra, dict):
            log_data.update(record.extra)
            
        # Format exception info if present
        if record.exc_info:
            log_data["exception"] = self.formatException(record.exc_info)
            
        return json.dumps(log_data)


def setup_logging():
    """Initializes and configures the root logger with the structured JSONFormatter handler."""
    root_logger = logging.getLogger()
    root_logger.setLevel(logging.INFO)
    
    # Clear any default pre-existing handlers
    for handler in root_logger.handlers[:]:
        root_logger.removeHandler(handler)
        
    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(JSONFormatter())
    root_logger.addHandler(stream_handler)
