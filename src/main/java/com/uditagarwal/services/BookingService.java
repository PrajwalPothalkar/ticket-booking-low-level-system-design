package com.uditagarwal.services;

import com.uditagarwal.config.BookingProperties;
import com.uditagarwal.exceptions.BadRequestException;
import com.uditagarwal.exceptions.NotFoundException;
import com.uditagarwal.exceptions.SeatPermanentlyUnavailableException;
import com.uditagarwal.model.Booking;
import com.uditagarwal.model.Seat;
import com.uditagarwal.model.Show;
import com.uditagarwal.providers.SeatLockProvider;
import com.uditagarwal.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatLockProvider seatLockProvider;
    private final BookingProperties bookingProperties;

    /**
     * One monitor per show. This makes "check already booked -> lock seats" atomic
     * for a single show without serializing bookings across all shows.
     */
    private final Map<String, Object> showMonitors = new ConcurrentHashMap<>();

    public Booking getBooking(final String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
    }

    public List<Booking> getAllBookings(final Show show) {
        return bookingRepository.findByShow(show);
    }

    public Booking createBooking(final String userId, final Show show, final List<Seat> seats) {
        validateSeatSelection(show, seats);

        synchronized (showMonitor(show.getId())) {
            if (isAnySeatAlreadyBooked(show, seats)) {
                throw new SeatPermanentlyUnavailableException();
            }
            // Throws SeatTemporaryUnavailableException if another user holds a lock.
            seatLockProvider.lockSeats(show, seats, userId);

            final Booking booking = new Booking(UUID.randomUUID().toString(), show, userId, seats);
            return bookingRepository.save(booking);
        }
    }

    public List<Seat> getBookedSeats(final Show show) {
        return getAllBookings(show).stream()
                .filter(Booking::isConfirmed)
                .flatMap(booking -> booking.getSeatsBooked().stream())
                .toList();
    }

    public void confirmBooking(final Booking booking, final String user) {
        requireOwner(booking, user);
        if (!booking.isCreated()) {
            throw new BadRequestException("Booking is not awaiting payment: " + booking.getBookingStatus());
        }
        for (Seat seat : booking.getSeatsBooked()) {
            if (!seatLockProvider.validateLock(booking.getShow(), seat, user)) {
                throw new BadRequestException(
                        "Seat lock expired or does not belong to user " + user + " for seat " + seat.getId());
            }
        }
        booking.confirmBooking();
        bookingRepository.save(booking);
        // Seats are now permanently unavailable through the confirmed booking, release the soft locks.
        seatLockProvider.unlockSeats(booking.getShow(), booking.getSeatsBooked(), user);
    }

    /**
     * Explicit "close UserBookingSession" before payment.
     */
    public void cancelBooking(final String bookingId, final String user) {
        final Booking booking = getBooking(bookingId);
        requireOwner(booking, user);
        expireBooking(booking);
    }

    public void expireBooking(final Booking booking) {
        booking.expireBooking();
        bookingRepository.save(booking);
        seatLockProvider.unlockSeats(booking.getShow(), booking.getSeatsBooked(), booking.getUser());
    }

    private void requireOwner(final Booking booking, final String user) {
        if (!booking.getUser().equals(user)) {
            throw new BadRequestException("Booking " + booking.getId() + " does not belong to user " + user);
        }
    }

    private void validateSeatSelection(final Show show, final List<Seat> seats) {
        if (seats.isEmpty()) {
            throw new BadRequestException("At least one seat must be selected");
        }
        if (seats.size() > bookingProperties.maxSeatsPerBooking()) {
            throw new BadRequestException("A user can book at most "
                    + bookingProperties.maxSeatsPerBooking() + " seats per booking");
        }
        if (new HashSet<>(seats).size() != seats.size()) {
            throw new BadRequestException("Duplicate seats in the booking request");
        }
        if (!show.getScreen().getSeats().containsAll(seats)) {
            throw new BadRequestException("All selected seats must belong to the show's screen");
        }
    }

    private boolean isAnySeatAlreadyBooked(final Show show, final List<Seat> seats) {
        final List<Seat> bookedSeats = getBookedSeats(show);
        return seats.stream().anyMatch(bookedSeats::contains);
    }

    private Object showMonitor(final String showId) {
        return showMonitors.computeIfAbsent(showId, ignored -> new Object());
    }
}
