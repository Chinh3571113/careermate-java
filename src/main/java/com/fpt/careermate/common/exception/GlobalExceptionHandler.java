package com.fpt.careermate.common.exception;

import com.fpt.careermate.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle client disconnection errors (browser closed, network drop, etc.)
     * These are expected when users close tabs or navigate away
     */
    @ExceptionHandler(value = AsyncRequestNotUsableException.class)
    ResponseEntity<ApiResponse> handlingAsyncRequestNotUsableException(AsyncRequestNotUsableException exception) {
        // Don't log full stack trace - this is normal behavior
        log.debug("Client disconnected: {}", exception.getMessage());
        // Return null - connection is already closed, can't send response
        return null;
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(Exception exception, HttpServletResponse response) {
        // Check if response is already committed (headers already sent to client)
        if (response != null && response.isCommitted()) {
            log.warn("Cannot send error response - response already committed. Exception: {}", 
                    exception.getClass().getSimpleName());
            return null; // Can't modify response, it's already sent
        }

        // Check for Tomcat RecycleRequiredException
        String exceptionClassName = exception.getClass().getName();
        if (exceptionClassName.contains("RecycleRequiredException")) {
            log.warn("Tomcat RecycleRequiredException - response handling conflict. " +
                    "This usually happens during authentication failures or filter chain issues.");
            return null; // Connection already closed/recycled
        }

        // Ignore client abort exceptions - these are normal when client closes
        // connection
        if (exception.getCause() != null &&
                exception.getCause().getClass().getName().contains("ClientAbortException")) {
            log.debug("Client aborted connection: {}", exception.getMessage());
            return null;
        }

        log.error("Exception: ", exception);
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception, HttpServletResponse response) {
        if (response != null && response.isCommitted()) {
            log.warn("Cannot send AppException response - already committed: {}", exception.getErrorCode());
            return null;
        }

        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception, HttpServletResponse response) {
        if (response != null && response.isCommitted()) {
            log.warn("Cannot send AccessDenied response - already committed");
            return null;
        }

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletResponse response) {
        if (response != null && response.isCommitted()) {
            log.warn("Cannot send validation error - already committed");
            return null;
        }

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Validation failed");
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleInvalidJson(HttpMessageNotReadableException ex, HttpServletResponse response) {
        if (response != null && response.isCommitted()) {
            log.warn("Cannot send JSON error - already committed");
            return null;
        }

        log.error("Invalid JSON format: ", ex);

        ErrorCode errorCode = ErrorCode.INVALID_JSON;
        String customMessage = errorCode.getMessage();

        // Check if the error is related to enum parsing
        String exceptionMessage = ex.getMessage();
        if (exceptionMessage != null) {
            // Check for enum parsing error
            if (exceptionMessage.contains("ResumeType") || exceptionMessage.contains("Cannot deserialize value")) {
                if (exceptionMessage.contains("ResumeType")) {
                    customMessage = "Invalid resume type. Accepted values: WEB, UPLOAD, DRAFT";
                } else if (exceptionMessage.contains("StatusJobApply")) {
                    customMessage = "Invalid job apply status. Accepted values: SUBMITTED, REVIEWING, APPROVED, REJECTED";
                } else if (exceptionMessage.contains("not one of the values accepted for Enum")) {
                    // Extract enum type and show valid values
                    customMessage = "Invalid enum value. Please check the accepted values for the field";
                }
            }
        }

        ApiResponse apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

}
