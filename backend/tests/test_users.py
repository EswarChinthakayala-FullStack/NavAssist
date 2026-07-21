import pytest
from app.models.safety import EmergencyContact

def test_emergency_contact_phone_property():
    """Verifies that phone property correctly maps to phone_number on EmergencyContact database model."""
    contact = EmergencyContact(name="John Doe", phone_number="+919876543210")
    
    # Assert property reading works
    assert contact.phone == "+919876543210"
    
    # Assert property writing works
    contact.phone = "+919999999999"
    assert contact.phone_number == "+919999999999"
    assert contact.phone == "+919999999999"
