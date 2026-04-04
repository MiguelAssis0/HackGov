package com.fiap.hackgov.infra.exceptions;

public class BlockedException extends RuntimeException {
    public BlockedException(String message) {
        super(message);
    }
}