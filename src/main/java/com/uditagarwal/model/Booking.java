package com.uditagarwal.model;

import com.uditagarwal.exceptions.InvalidStateException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode(of = "id")
public class Booking {

    private final String id;
    private final Show show;
    private final List<Seat> seatsBooked;
    private final String user;
    private final Instant createdAt;
    private BookingStatus bookingStatus;

    public Booking(@NonNull final String id, @NonNull final Show show, @NonNull final String user,
                   @NonNull final List<Seat> seatsBooked) {
        this(id, show, user, seatsBooked, Instant.now());
    }

    public Booking(@NonNull final String id, @NonNull final Show show, @NonNull final String user,
                   @NonNull final List<Seat> seatsBooked, @NonNull final Instant createdAt) {
        this.id = id;
        this.show = show;
        this.seatsBooked = List.copyOf(seatsBooked);
        this.user = user;
        this.createdAt = createdAt;
        this.bookingStatus = BookingStatus.CREATED;
    }

    public List<Seat> getSeatsBooked() {
        return Collections.unmodifiableList(seatsBooked);
    }

    public boolean isConfirmed() {
        return this.bookingStatus == BookingStatus.CONFIRMED;
    }

    public boolean isCreated() {
        return this.bookingStatus == BookingStatus.CREATED;
    }

    public void confirmBooking() {
        if (!isCreated()) {
            throw new InvalidStateException("Only a CREATED booking can be confirmed, current status: " + bookingStatus);
        }
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public void expireBooking() {
        if (!isCreated()) {
            throw new InvalidStateException("Only a CREATED booking can be expired, current status: " + bookingStatus);
        }
        this.bookingStatus = BookingStatus.EXPIRED;
    }
}
