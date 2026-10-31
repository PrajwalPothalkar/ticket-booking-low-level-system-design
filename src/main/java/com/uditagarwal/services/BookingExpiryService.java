package com.uditagarwal.services;

import com.uditagarwal.config.BookingProperties;
import com.uditagarwal.exceptions.InvalidStateException;
import com.uditagarwal.model.Booking;
import com.uditagarwal.providers.SeatLockProvider;
import com.uditagarwal.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Closes UserBookingSessions that were never paid for (the bonus requirement)
 * and drops expired seat locks so memory stays bounded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingExpiryService {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final SeatLockProvider seatLockProvider;
    private final BookingProperties bookingProperties;

    @Scheduled(fixedDelayString = "${booking.expiry-scan-interval-ms:30000}")
    public void expireStaleBookings() {
        expireBookingsCreatedBefore(Instant.now().minusSeconds(bookingProperties.lockTimeoutSeconds()));
        seatLockProvider.cleanupExpiredLocks();
    }

    /**
     * Extracted so the expiry rule can be tested deterministically without sleeping.
     *
     * @return ids of the bookings that were expired by this call
     */
    public List<String> expireBookingsCreatedBefore(final Instant cutoff) {
        final List<String> expiredBookingIds = new ArrayList<>();

        for (Booking booking : bookingRepository.findAll()) {
            if (!booking.isCreated() || !booking.getCreatedAt().isBefore(cutoff)) {
                continue;
            }
            try {
                bookingService.expireBooking(booking);
                expiredBookingIds.add(booking.getId());
            } catch (InvalidStateException raceLostToPayment) {
                // The booking was confirmed between the check and the update; that is fine.
                log.debug("Skipping booking {}: {}", booking.getId(), raceLostToPayment.getMessage());
            }
        }

        if (!expiredBookingIds.isEmpty()) {
            log.info("Expired {} stale booking(s)", expiredBookingIds.size());
        }
        return expiredBookingIds;
    }
}
