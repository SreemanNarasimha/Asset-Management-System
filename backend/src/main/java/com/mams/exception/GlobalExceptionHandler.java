package com.mams.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), determineStatus(ex.getErrorCode()), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, "Validation failed", HttpStatus.BAD_REQUEST, fieldErrors);
    }
    
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, "Cannot delete this record because it is referenced by other records.", HttpStatus.BAD_REQUEST, null);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        ex.printStackTrace();
        return buildErrorResponse(ErrorCode.TRANSACTION_FAILED, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(ErrorCode errorCode, String message, HttpStatus status, Map<String, String> fieldErrors) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("errorCode", errorCode.name());
        body.put("message", message);
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            body.put("fieldErrors", fieldErrors);
        }
        return new ResponseEntity<>(body, status);
    }

    private HttpStatus determineStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_ERROR, INVALID_TRANSFER -> HttpStatus.BAD_REQUEST;
            case INSUFFICIENT_INVENTORY, DUPLICATE_REFERENCE -> HttpStatus.CONFLICT;
            case TRANSACTION_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
