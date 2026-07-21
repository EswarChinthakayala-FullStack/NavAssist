class AppException(Exception):
    """Base application exception for custom backend errors."""
    def __init__(self, message: str, status_code: int = 400):
        super().__init__(message)
        self.message = message
        self.status_code = status_code


class NotFoundError(AppException):
    """Raised when a requested resource is not found in the database."""
    def __init__(self, message: str = "Resource not found"):
        super().__init__(message, status_code=404)


class ConflictError(AppException):
    """Raised when an operation conflicts with existing server state (e.g. duplicate keys)."""
    def __init__(self, message: str = "Resource state conflict"):
        super().__init__(message, status_code=409)


class PermissionDeniedError(AppException):
    """Raised when a user lacks required permissions to access a resource."""
    def __init__(self, message: str = "Permission denied"):
        super().__init__(message, status_code=403)


class ValidationError(AppException):
    """Raised when request payload validation fails."""
    def __init__(self, message: str = "Validation failed"):
        super().__init__(message, status_code=422)


class PaymentError(AppException):
    """Raised when payment gateway interactions fail or checks mismatch."""
    def __init__(self, message: str = "Payment processing failed"):
        super().__init__(message, status_code=400)


class BookingError(AppException):
    """Raised when invalid state machine transitions or booking lifecycle errors occur."""
    def __init__(self, message: str = "Booking processing error"):
        super().__init__(message, status_code=400)
