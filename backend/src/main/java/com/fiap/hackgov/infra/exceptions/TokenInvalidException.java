package com.fiap.hackgov.infra.exceptions;

public class TokenInvalidException extends RuntimeException {
    public TokenInvalidException(String message) {
        super(message);
    }
}
