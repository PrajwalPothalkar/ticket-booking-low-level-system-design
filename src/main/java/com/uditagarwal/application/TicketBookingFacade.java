package com.uditagarwal.application;

import com.uditagarwal.model.Booking;
import com.uditagarwal.model.Movie;
import com.uditagarwal.model.Payment;
import com.uditagarwal.model.Screen;
import com.uditagarwal.model.Seat;
import com.uditagarwal.model.Show;
import com.uditagarwal.model.Theatre;
import com.uditagarwal.services.BookingService;
import com.uditagarwal.services.MovieService;
import com.uditagarwal.services.PaymentsService;
import com.uditagarwal.services.SeatAvailabilityService;
import com.uditagarwal.services.ShowService;
import com.uditagarwal.services.TheatreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Use-case boundary of the application.
 *
 * This is the equivalent of the old controllers, minus HTTP: it resolves ids,
 * orchestrates the services and returns domain objects. The demo (and any future
 * transport) talks to this class instead of juggling six services.
 */
@Component
@RequiredArgsConstructor
public class TicketBookingFacade {

    private final MovieService movieService;
    private final TheatreService theatreService;
    private final ShowService showService;
    private final BookingService bookingService;
    private final PaymentsService paymentsService;
    private final SeatAvailabilityService seatAvailabilityService;

    public Movie createMovie(final String movieName) {
        return movieService.createMovie(movieName);
    }

    public Theatre createTheatre(final String theatreName) {
        return theatreService.createTheatre(theatreName);
    }

    public Screen createScreen(final String theatreId, final String screenName) {
        final Theatre theatre = theatreService.getTheatre(theatreId);
        return theatreService.createScreenInTheatre(screenName, theatre);
    }

    public Seat addSeat(final String screenId, final int rowNo, final int seatNo) {
        final Screen screen = theatreService.getScreen(screenId);
        return theatreService.createSeatInScreen(rowNo, seatNo, screen);
    }

    public Show createShow(final String movieId, final String screenId,
                           final Instant startTime, final int durationInSeconds) {
        final Movie movie = movieService.getMovie(movieId);
        final Screen screen = theatreService.getScreen(screenId);
        return showService.createShow(movie, screen, startTime, durationInSeconds);
    }

    public List<Seat> availableSeats(final String showId) {
        final Show show = showService.getShow(showId);
        return seatAvailabilityService.getAvailableSeats(show);
    }

    public Booking book(final String userId, final String showId, final List<String> seatIds) {
        final Show show = showService.getShow(showId);
        final List<Seat> seats = seatIds.stream()
                .map(theatreService::getSeat)
                .toList();
        return bookingService.createBooking(userId, show, seats);
    }

    public Booking getBooking(final String bookingId) {
        return bookingService.getBooking(bookingId);
    }

    public List<Payment> getPayments(final String bookingId) {
        return paymentsService.getPayments(bookingId);
    }

    public Payment paymentSuccess(final String bookingId, final String userId) {
        return paymentsService.paymentSucceeded(bookingId, userId);
    }

    public Payment paymentFailure(final String bookingId, final String userId) {
        return paymentsService.paymentFailed(bookingId, userId);
    }

    /**
     * Explicitly closes the UserBookingSession before payment.
     */
    public void cancelBooking(final String bookingId, final String userId) {
        bookingService.cancelBooking(bookingId, userId);
    }
}
