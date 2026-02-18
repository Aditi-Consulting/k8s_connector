package com.AI_Project.K8sConnectorApplication.exception;

import org.springframework.http.HttpStatus;

/**
 * Simple runtime exception for Kubernetes connector operations.
 * Carries an HttpStatus so the REST layer can respond appropriately.
 */
public class K8sConnectorException extends RuntimeException {
    private final HttpStatus status;

    // Primary constructors
    public K8sConnectorException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public K8sConnectorException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    // Backward compatible convenience constructors (default to 500)
    public K8sConnectorException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public K8sConnectorException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // Convenience factory methods for common cases
    public static K8sConnectorException notFound(String message) {
        return new K8sConnectorException(HttpStatus.NOT_FOUND, message);
    }

    public static K8sConnectorException badRequest(String message) {
        return new K8sConnectorException(HttpStatus.BAD_REQUEST, message);
    }

    public static K8sConnectorException conflict(String message) {
        return new K8sConnectorException(HttpStatus.CONFLICT, message);
    }

    public static K8sConnectorException serverError(String message, Throwable cause) {
        return new K8sConnectorException(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
