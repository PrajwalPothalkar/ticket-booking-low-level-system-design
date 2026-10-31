package com.uditagarwal.exceptions;

public class SeatTemporaryUnavailableException extends RuntimeException {

    public SeatTemporaryUnavailableException() {
        super("One or more of the selected seats are not available at this moment");
    }

    public SeatTemporaryUnavailableException(final String message) {
        super(message);
    }
}
