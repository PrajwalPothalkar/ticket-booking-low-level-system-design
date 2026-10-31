package com.uditagarwal.providers;

import com.uditagarwal.model.Seat;
import com.uditagarwal.model.Show;

import java.util.List;

public interface SeatLockProvider {

    void lockSeats(Show show, List<Seat> seats, String user);

    void unlockSeats(Show show, List<Seat> seats, String user);

    boolean validateLock(Show show, Seat seat, String user);

    List<Seat> getLockedSeats(Show show);

    /**
     * Removes expired locks so the in-memory state does not grow forever.
     */
    void cleanupExpiredLocks();
}
