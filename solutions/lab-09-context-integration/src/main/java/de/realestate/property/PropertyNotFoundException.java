package de.realestate.property;

public class PropertyNotFoundException extends RuntimeException {

    private final Long propertyId;

    public PropertyNotFoundException(Long propertyId) {
        super("No property with ID %d exists".formatted(propertyId));
        this.propertyId = propertyId;
    }

    public Long getPropertyId() {
        return propertyId;
    }

}
