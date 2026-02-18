package com.AI_Project.K8sConnectorApplication.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(K8sConnectorException.class)
    public ResponseEntity<?> handleK8sConnectorException(K8sConnectorException ex) {
        logger.error("K8sConnectorException: status={}, message={}", ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(ex.getStatus().getReasonPhrase(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500)
                .body(new ErrorResponse("Internal Server Error", ex.getMessage()));
    }

    @Getter
    @AllArgsConstructor
    static class ErrorResponse {
        private String error;
        private String message;
    }
}