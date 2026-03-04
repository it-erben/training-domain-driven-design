package de.immobiliencrm.vermittlung.domain.model;

/**
 * Base exception class for domain-level errors.
 * This exception is Spring-free and belongs to the domain layer.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
