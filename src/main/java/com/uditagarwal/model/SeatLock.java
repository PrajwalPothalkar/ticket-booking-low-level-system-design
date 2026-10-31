package com.uditagarwal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class SeatLock {

    private final Seat seat;
    private final Show show;
    private final int timeoutInSeconds;
    private final Instant lockTime;
    private final String lockedBy;

    public boolean isLockExpired(@NonNull final Instant now) {
        return lockTime.plusSeconds(timeoutInSeconds).isBefore(now);
    }

    public boolean isLockExpired() {
        return isLockExpired(Instant.now());
    }
}
