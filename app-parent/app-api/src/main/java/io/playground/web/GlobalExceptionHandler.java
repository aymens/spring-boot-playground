package io.playground.web;

import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        String message = switch (paramName) {
            case String s when s.toLowerCase().matches(".*date.*") -> "Invalid date format. Use: yyyy-MM-dd, yyyy-MM-dd HH, or yyyy-MM-dd HH:mm";
            default -> e.getMessage();
        };
        return badRequest().body(Map.of(paramName, message));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleBusinessException(NotFoundException e) {
        return status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusinessException(BusinessException e) {
        return badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException e) {
        return badRequest().body(
                e.getBindingResult().getFieldErrors().stream()
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                this::fieldErrorMessageExtractor)));

//        Map<String, String> errors = new HashMap<>();
//        e.getBindingResult().getFieldErrors()
//                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
//        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Map<String, String>> handlePropertyReferenceException(PropertyReferenceException e) {
        return badRequest().body(Map.of(e.getPropertyName(), e.getMessage()));
    }

    private String fieldErrorMessageExtractor(FieldError fieldError) {
        return Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
    }
}