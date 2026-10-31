package com.uditagarwal.exceptions;

public class ScreenAlreadyOccupiedException extends RuntimeException {

    public ScreenAlreadyOccupiedException(final String message) {
        super(message);
    }
}
