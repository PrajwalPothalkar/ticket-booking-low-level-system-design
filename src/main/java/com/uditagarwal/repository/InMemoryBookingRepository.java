package com.uditagarwal.repository;

import com.uditagarwal.model.Booking;
import com.uditagarwal.model.Show;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookingRepository implements BookingRepository {

    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    @Override
    public Booking save(final Booking booking) {
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(final String bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    @Override
    public List<Booking> findByShow(final Show show) {
        return bookings.values().stream()
                .filter(booking -> booking.getShow().equals(show))
                .toList();
    }

    @Override
    public List<Booking> findAll() {
        return List.copyOf(bookings.values());
    }
}
