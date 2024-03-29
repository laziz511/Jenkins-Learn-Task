package com.epam.esm.web.exception;

import com.epam.esm.core.dto.ApiErrorResponseDTO;
import com.epam.esm.core.entity.GiftCertificate;
import com.epam.esm.core.entity.Order;
import com.epam.esm.core.entity.Tag;
import com.epam.esm.core.entity.User;
import com.epam.esm.core.exception.AuthException;
import com.epam.esm.core.exception.DuplicateEntityException;
import com.epam.esm.core.exception.NotFoundException;
import com.epam.esm.core.exception.OperationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.epam.esm.core.constants.ErrorCodeConstants.*;

/**
 * Global exception handler for handling specific exceptions and providing consistent API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<Class<?>, Integer> NOT_FOUND_ERROR_CODES = new HashMap<>();
    private static final Map<Class<?>, Integer> OPERATION_ERROR_CODES = new HashMap<>();

    static {
        NOT_FOUND_ERROR_CODES.put(GiftCertificate.class, GIFT_CERTIFICATE_NOT_FOUND);
        NOT_FOUND_ERROR_CODES.put(Order.class, ORDER_NOT_FOUND);
        NOT_FOUND_ERROR_CODES.put(Tag.class, TAG_NOT_FOUND);
        NOT_FOUND_ERROR_CODES.put(User.class, USER_NOT_FOUND);

        OPERATION_ERROR_CODES.put(GiftCertificate.class, GIFT_CERTIFICATE_OPERATION_ERROR);
        OPERATION_ERROR_CODES.put(Order.class, ORDER_OPERATION_ERROR);
        OPERATION_ERROR_CODES.put(Tag.class, TAG_OPERATION_ERROR);
        OPERATION_ERROR_CODES.put(User.class, USER_OPERATION_ERROR);
    }

    /**
     * Handles NotFoundException and returns a ResponseEntity with an appropriate API error response.
     *
     * @param ex  The NotFoundException that occurred.
     * @param req The HttpServletRequest associated with the request.
     * @return ResponseEntity containing an API error response.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleNotFoundException(NotFoundException ex, HttpServletRequest req) {
        int errorCode = NOT_FOUND_ERROR_CODES.getOrDefault(ex.getExceptionClass(), 0);
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, errorCode);
    }

    /**
     * Handles OperationException and returns a ResponseEntity with an appropriate API error response.
     *
     * @param ex  The OperationException that occurred.
     * @param req The HttpServletRequest associated with the request.
     * @return ResponseEntity containing an API error response.
     */
    @ExceptionHandler(OperationException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleOperationException(OperationException ex, HttpServletRequest req) {
        int errorCode = OPERATION_ERROR_CODES.getOrDefault(ex.getExceptionClass(), 0);
        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, errorCode);
    }

    /**
     * Handles MethodArgumentNotValidException and returns a ResponseEntity with an appropriate API error response.
     *
     * @param ex The MethodArgumentNotValidException that occurred.
     * @return ResponseEntity containing an API error response.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST, VALIDATION_ERROR);
    }

    /**
     * Handles HttpMessageNotReadableException and returns a ResponseEntity with an appropriate API error response.
     *
     * @param ex The HttpMessageNotReadableException that occurred.
     * @return ResponseEntity containing an API error response.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String errorMessage = "Required field is missing in JSON object";
        return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST, FIELD_MISSING_IN_PATCH_REQUEST_ERROR);
    }

    /**
     * Handles AuthenticationException, such as BadCredentialsException, and returns a ResponseEntity
     * with an appropriate API error response for unauthorized access.
     *
     * @param e The AuthenticationException that occurred.
     * @return ResponseEntity containing an API error response with details about the authentication failure.
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiErrorResponseDTO> handleAuthenticationException(AuthenticationException e) {
        String errorMessage = (e instanceof BadCredentialsException) ? "Invalid username or password" : "Authentication failed";

        return buildErrorResponse(errorMessage, HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR);
    }


    /**
     * Handles AuthException and returns a ResponseEntity with an appropriate API error response.
     *
     * @param ex The AuthException that occurred.
     * @return ResponseEntity containing an API error response.
     */
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiErrorResponseDTO> handleAuthException(AuthException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, AUTHENTICATION_ERROR);
    }


    /**
     * Handles DuplicateEntityException and returns a ResponseEntity with an appropriate API error response.
     *
     * @param ex The DuplicateEntityException that occurred.
     * @return ResponseEntity containing an API error response.
     */
    @ExceptionHandler(DuplicateEntityException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiErrorResponseDTO> handleDuplicateEntityException(DuplicateEntityException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, DUPLICATE_ENTITY_ERROR);
    }

    private ResponseEntity<ApiErrorResponseDTO> buildErrorResponse(String message, HttpStatus status, int errorCode) {
        ApiErrorResponseDTO apiError = new ApiErrorResponseDTO(message, errorCode);
        return ResponseEntity.status(status).body(apiError);
    }
}
