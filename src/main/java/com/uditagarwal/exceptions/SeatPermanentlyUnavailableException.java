package com.uditagarwal.exceptions;

public class SeatPermanentlyUnavailableException extends RuntimeException {

    public SeatPermanentlyUnavailableException() {
        super("One or more of the selected seats are already booked for this show");
    }

    public SeatPermanentlyUnavailableException(final String message) {
        super(message);
    }
}
