package com.uditagarwal.exceptions;

public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(final String message) {
        super(message);
    }
}
