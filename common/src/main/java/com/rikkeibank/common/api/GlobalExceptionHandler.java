package com.rikkeibank.common.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant; import java.util.LinkedHashMap; import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ApiError> business(BusinessException ex, HttpServletRequest req) { return response(ex.status(), ex.code(), ex.getMessage(), req, Map.of()); }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String,String> fields = new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
    return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", req, fields);
  }
  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ApiError> constraint(ConstraintViolationException ex, HttpServletRequest req) { return response(HttpStatus.BAD_REQUEST,"VALIDATION_ERROR",ex.getMessage(),req,Map.of()); }
  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> denied(AccessDeniedException ex, HttpServletRequest req) { return response(HttpStatus.FORBIDDEN,"ACCESS_DENIED","You are not allowed to perform this action",req,Map.of()); }
  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unknown(Exception ex, HttpServletRequest req) { log.error("Unhandled error for {}", req.getRequestURI(), ex); return response(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","An unexpected error occurred",req,Map.of()); }
  private ResponseEntity<ApiError> response(HttpStatus s,String c,String m,HttpServletRequest r,Map<String,String> v) { return ResponseEntity.status(s).body(new ApiError(Instant.now(),s.value(),s.getReasonPhrase(),c,m,r.getRequestURI(),v)); }
}

