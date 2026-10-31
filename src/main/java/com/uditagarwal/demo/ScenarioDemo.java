package com.uditagarwal.demo;

import com.uditagarwal.application.TicketBookingFacade;
import com.uditagarwal.exceptions.SeatPermanentlyUnavailableException;
import com.uditagarwal.exceptions.SeatTemporaryUnavailableException;
import com.uditagarwal.model.*;
import com.uditagarwal.services.BookingExpiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The three problem-statement scenarios plus the timeout bonus, printed in order.
 * This is the only "entry point" of the app now - no HTTP, no test framework.
 */
@Component
@RequiredArgsConstructor
public class ScenarioDemo {

    private static final String USER_1 = "User1";
    private static final String USER_2 = "User2";
    private static final String USER_3 = "User3";

    private final TicketBookingFacade facade;
    private final BookingExpiryService bookingExpiryService;

    private int passed;
    private int failed;

    public DemoReport runAll() {
        passed = 0;
        failed = 0;

        banner();
        runCase1();
        runCase2();
        runCase3();
        runTimeoutBonus();

        System.out.println();
        System.out.println("==============================================================");
        System.out.printf(" Result: %d passed, %d failed%n", passed, failed);
        System.out.println("==============================================================");
        return new DemoReport(passed, failed);
    }

    private void runCase1() {
        section("CASE 1 - U1 books, U2 must not see those seats");

        final Fixture fixture = fixture(2, 10);
        final List<String> u1Seats = pick(fixture, 0, 2, 5, 10);

        log("U1 sees " + available(fixture).size() + " available seats");
        final Booking booking = facade.book(USER_1, fixture.showId(), u1Seats);
        log("U1 booked " + u1Seats.size() + " seats (booking=" + shortId(booking) + ")");
        check("U2 does not see U1's locked seats", unavailable(fixture, u1Seats));

        facade.paymentSuccess(booking.getId(), USER_1);
        log("payment SUCCESS for " + USER_1);

        check("U2 sees 16 seats (20 total - 4 booked)", available(fixture).size() == 16);
        check("U2 still cannot see U1's seats", unavailable(fixture, u1Seats));
        check("booking is CONFIRMED", status(booking) == BookingStatus.CONFIRMED);
    }

    private void runCase2() {
        section("CASE 2 - payment fails / session cancelled, seats come back");

        final Fixture fixture = fixture(2, 10);
        final List<String> u1Seats = pick(fixture, 0, 2, 5, 10);

        final Booking failedBooking = facade.book(USER_1, fixture.showId(), u1Seats);
        log("U1 booked " + u1Seats.size() + " seats (booking=" + shortId(failedBooking) + ")");
        check("U2 sees 16 seats while U1 is paying", available(fixture).size() == 16);

        facade.paymentFailure(failedBooking.getId(), USER_1);
        log("payment FAILED for " + USER_1 + " (allowedRetries=0)");
        check("U2 sees all 20 seats after retries are exhausted", available(fixture).size() == 20);
        check("booking is EXPIRED", status(failedBooking) == BookingStatus.EXPIRED);

        final Booking cancelledBooking = facade.book(USER_1, fixture.showId(), u1Seats);
        log("U1 booked again (booking=" + shortId(cancelledBooking) + ")");
        check("U2 sees 16 seats before explicit cancel", available(fixture).size() == 16);

        facade.cancelBooking(cancelledBooking.getId(), USER_1);
        log("U1 explicitly closed the booking session");
        check("U2 sees all 20 seats after explicit cancel", available(fixture).size() == 20);
        check("cancelled booking is EXPIRED", status(cancelledBooking) == BookingStatus.EXPIRED);
    }

    private void runCase3() {
        section("CASE 3 - two users fight over overlapping seats");

        final Fixture fixture = fixture(2, 10);
        final List<String> u1Seats = pick(fixture, 0, 2, 5, 10);
        final List<String> u2Seats = pick(fixture, 0, 1, 2, 3);

        final Booking u1Booking = facade.book(USER_1, fixture.showId(), u1Seats);
        log("U1 booked " + u1Seats.size() + " seats (booking=" + shortId(u1Booking) + ")");

        checkThrows("U2 is rejected while U1 holds the lock",
                SeatTemporaryUnavailableException.class,
                () -> facade.book(USER_2, fixture.showId(), u2Seats));

        facade.paymentSuccess(u1Booking.getId(), USER_1);
        log("payment SUCCESS for " + USER_1);

        checkThrows("U2 is rejected again because U1's seats are confirmed",
                SeatPermanentlyUnavailableException.class,
                () -> facade.book(USER_2, fixture.showId(), u2Seats));
        check("U1's booking is CONFIRMED", status(u1Booking) == BookingStatus.CONFIRMED);
    }

    private void runTimeoutBonus() {
        section("BONUS - unpaid UserBookingSession times out and releases seats");

        final Fixture fixture = fixture(2, 10);
        final List<String> seats = pick(fixture, 0);

        final Booking booking = facade.book(USER_3, fixture.showId(), seats);
        log("U3 booked 1 seat and never paid (booking=" + shortId(booking) + ")");
        check("seat is unavailable while the session is open", available(fixture).size() == 19);

        // Simulate the configured lock timeout without sleeping in the demo.
        final List<String> expired = bookingExpiryService
                .expireBookingsCreatedBefore(Instant.now().plusSeconds(1));

        check("the stale session was expired", expired.contains(booking.getId()));
        check("booking is EXPIRED", status(booking) == BookingStatus.EXPIRED);
        check("seat is available again", available(fixture).size() == 20);
    }

    private Fixture fixture(final int rows, final int seatsPerRow) {
        final Movie movie = facade.createMovie("Interstellar");
        final Theatre theatre = facade.createTheatre("PVR Orion");
        final Screen screen = facade.createScreen(theatre.getId(), "Screen 1");

        final List<String> seatIds = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int seatNo = 0; seatNo < seatsPerRow; seatNo++) {
                seatIds.add(facade.addSeat(screen.getId(), row, seatNo).getId());
            }
        }

        final Show show = facade.createShow(
                movie.getId(), screen.getId(), Instant.now().plusSeconds(3600), 2 * 60 * 60);
        return new Fixture(show.getId(), seatIds);
    }

    private List<String> available(final Fixture fixture) {
        return facade.availableSeats(fixture.showId()).stream()
                .map(Seat::getId)
                .toList();
    }

    private boolean unavailable(final Fixture fixture, final List<String> seats) {
        final List<String> availableSeats = available(fixture);
        return seats.stream().noneMatch(availableSeats::contains);
    }

    private List<String> pick(final Fixture fixture, final int... indexes) {
        final List<String> seats = new ArrayList<>();
        for (int index : indexes) {
            seats.add(fixture.seatIds().get(index));
        }
        return seats;
    }

    private BookingStatus status(final Booking booking) {
        return facade.getBooking(booking.getId()).getBookingStatus();
    }

    private String shortId(final Booking booking) {
        return booking.getId().substring(0, 8);
    }

    private void checkThrows(final String label, final Class<? extends RuntimeException> expected,
                             final Runnable action) {
        try {
            action.run();
            check(label + " (no exception was thrown)", false);
        } catch (RuntimeException exception) {
            check(label + " [" + exception.getClass().getSimpleName() + "]", expected.isInstance(exception));
        }
    }

    private void check(final String label, final boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label);
        }
    }

    private void log(final String message) {
        System.out.println("  ... " + message);
    }

    private void section(final String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    private void banner() {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println(" Movie Ticket Booking - LLD scenario demo");
        System.out.println("==============================================================");
    }

    private record Fixture(String showId, List<String> seatIds) {
    }

    public record DemoReport(int passed, int failed) {
        public boolean isSuccess() {
            return failed == 0;
        }
    }
}
