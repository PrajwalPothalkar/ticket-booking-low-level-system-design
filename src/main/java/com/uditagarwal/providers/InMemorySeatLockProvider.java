package com.uditagarwal.providers;

import com.uditagarwal.config.BookingProperties;
import com.uditagarwal.exceptions.SeatTemporaryUnavailableException;
import com.uditagarwal.model.Seat;
import com.uditagarwal.model.SeatLock;
import com.uditagarwal.model.Show;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory seat lock store.
 *
 * Every public method is synchronized because a lock store is read and written
 * concurrently. Mutating a HashMap from multiple threads (the previous version)
 * could corrupt its internal state or spin forever.
 */
@Component
public class InMemorySeatLockProvider implements SeatLockProvider {

    private final int lockTimeoutSeconds;
    private final Map<Show, Map<Seat, SeatLock>> locks = new ConcurrentHashMap<>();

    public InMemorySeatLockProvider(final BookingProperties bookingProperties) {
        this.lockTimeoutSeconds = bookingProperties.lockTimeoutSeconds();
    }

    @Override
    public synchronized void lockSeats(@NonNull final Show show, @NonNull final List<Seat> seats,
                                       @NonNull final String user) {
        for (Seat seat : seats) {
            if (isSeatLocked(show, seat)) {
                throw new SeatTemporaryUnavailableException();
            }
        }
        for (Seat seat : seats) {
            locks.computeIfAbsent(show, ignored -> new HashMap<>())
                    .put(seat, new SeatLock(seat, show, lockTimeoutSeconds, Instant.now(), user));
        }
    }

    @Override
    public synchronized void unlockSeats(@NonNull final Show show, @NonNull final List<Seat> seats,
                                         @NonNull final String user) {
        for (Seat seat : seats) {
            if (validateLock(show, seat, user)) {
                unlockSeat(show, seat);
            }
        }
    }

    @Override
    public synchronized boolean validateLock(@NonNull final Show show, @NonNull final Seat seat,
                                             @NonNull final String user) {
        final Map<Seat, SeatLock> seatLocks = locks.get(show);
        if (seatLocks == null) {
            return false;
        }
        final SeatLock lock = seatLocks.get(seat);
        return lock != null && !lock.isLockExpired() && lock.getLockedBy().equals(user);
    }

    @Override
    public synchronized List<Seat> getLockedSeats(@NonNull final Show show) {
        final Map<Seat, SeatLock> seatLocks = locks.get(show);
        if (seatLocks == null) {
            return List.of();
        }
        final List<Seat> lockedSeats = new ArrayList<>();
        final Instant now = Instant.now();
        for (Map.Entry<Seat, SeatLock> entry : seatLocks.entrySet()) {
            if (!entry.getValue().isLockExpired(now)) {
                lockedSeats.add(entry.getKey());
            }
        }
        return lockedSeats;
    }

    @Override
    public synchronized void cleanupExpiredLocks() {
        final Instant now = Instant.now();
        locks.entrySet().removeIf(showEntry -> {
            showEntry.getValue().entrySet().removeIf(lockEntry -> lockEntry.getValue().isLockExpired(now));
            return showEntry.getValue().isEmpty();
        });
    }

    private void unlockSeat(final Show show, final Seat seat) {
        final Map<Seat, SeatLock> seatLocks = locks.get(show);
        if (seatLocks == null) {
            return;
        }
        seatLocks.remove(seat);
        if (seatLocks.isEmpty()) {
            locks.remove(show);
        }
    }

    private boolean isSeatLocked(final Show show, final Seat seat) {
        final Map<Seat, SeatLock> seatLocks = locks.get(show);
        if (seatLocks == null) {
            return false;
        }
        final SeatLock lock = seatLocks.get(seat);
        return lock != null && !lock.isLockExpired();
    }
}
