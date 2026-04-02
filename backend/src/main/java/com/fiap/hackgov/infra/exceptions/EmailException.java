package com.fiap.hackgov.infra.exceptions;

public class EmailException extends RuntimeException {
    public EmailException(String message) {
        super(message);
    }
}