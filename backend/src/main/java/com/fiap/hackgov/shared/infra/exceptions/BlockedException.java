package com.fiap.hackgov.shared.infra.exceptions;

public class BlockedException extends RuntimeException {
    public BlockedException(String message) {
        super(message);
    }
}