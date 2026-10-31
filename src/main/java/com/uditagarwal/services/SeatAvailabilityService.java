package com.uditagarwal.services;

import com.uditagarwal.model.Seat;
import com.uditagarwal.model.Show;
import com.uditagarwal.providers.SeatLockProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SeatAvailabilityService {

    private final BookingService bookingService;
    private final SeatLockProvider seatLockProvider;

    public List<Seat> getAvailableSeats(final Show show) {
        final Set<Seat> unavailableSeats = new HashSet<>(bookingService.getBookedSeats(show));
        unavailableSeats.addAll(seatLockProvider.getLockedSeats(show));

        return show.getScreen().getSeats().stream()
                .filter(seat -> !unavailableSeats.contains(seat))
                .toList();
    }
}
