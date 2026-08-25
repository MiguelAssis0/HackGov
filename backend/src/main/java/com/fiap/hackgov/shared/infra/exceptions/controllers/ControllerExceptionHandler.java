package com.fiap.hackgov.shared.infra.exceptions.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fiap.hackgov.shared.infra.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        return handleException("Resource Not Found", HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e, HttpServletRequest request) {
        return handleException("Resource Already Exists", HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ProblemDetail> handleAuthException(AuthException e, HttpServletRequest request) {
        return handleException("Auth Exception", HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException e, HttpServletRequest request) {
        return handleException("Business Exception", HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<ProblemDetail> handleJWTVerificationException(JWTVerificationException e, HttpServletRequest request) {
        return handleException("JWT Verification Exception", HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentialsException(InvalidCredentialsException e, HttpServletRequest request) {
        return handleException("Invalid Credentials", HttpStatus.UNAUTHORIZED, e, request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        return handleException("Unauthorized", HttpStatus.UNAUTHORIZED, e, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        return handleException("Forbidden", HttpStatus.FORBIDDEN, e, request);
    }

    @ExceptionHandler(BlockedException.class)
    public ResponseEntity<ProblemDetail> handleBlockedException(BlockedException e, HttpServletRequest request) {
        return handleException("Too Many Requests", HttpStatus.TOO_MANY_REQUESTS, e, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errors = e.getBindingResult().getAllErrors().stream().map(error -> {
            if (error instanceof FieldError fieldError) {
                return fieldError.getField() + ": " + fieldError.getDefaultMessage();
            }

            return error.getDefaultMessage();
        }).toList();

        ProblemDetail problem = createProblemDetail("Dados inválidos", HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.", request);

        problem.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException e, HttpServletRequest request) {
        return handleException("Argument Not Valid", HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        return handleException("Argument Not Valid", HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        return handleException("Argument Not Valid", HttpStatus.BAD_REQUEST, e, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return handleException("Method Not Valid", HttpStatus.METHOD_NOT_ALLOWED, e, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        return handleException("Invalid Url", HttpStatus.NOT_FOUND, e, request);
    }

    private ResponseEntity<ProblemDetail> handleException(String title, HttpStatus status, Exception e, HttpServletRequest request) {
        ProblemDetail problem = createProblemDetail(title, status, e.getMessage(), request);

        return ResponseEntity.status(status).body(problem);
    }

    private ProblemDetail createProblemDetail(String title, HttpStatus status, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}
