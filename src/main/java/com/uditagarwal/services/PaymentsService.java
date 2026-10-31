package com.uditagarwal.services;

import com.uditagarwal.config.PaymentProperties;
import com.uditagarwal.exceptions.BadRequestException;
import com.uditagarwal.exceptions.InvalidStateException;
import com.uditagarwal.model.Booking;
import com.uditagarwal.model.Payment;
import com.uditagarwal.model.PaymentStatus;
import com.uditagarwal.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PaymentsService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final PaymentProperties paymentProperties;

    /** Prevents double-confirmation when the same payment callback is retried concurrently. */
    private final Map<String, Object> bookingMonitors = new ConcurrentHashMap<>();

    public Payment paymentSucceeded(final String bookingId, final String user) {
        synchronized (monitorFor(bookingId)) {
            final Booking booking = bookingService.getBooking(bookingId);
            requireOwner(booking, user);

            final Optional<Payment> existingSuccess = paymentRepository.findSuccessfulByBookingId(bookingId);
            if (existingSuccess.isPresent()) {
                return existingSuccess.get();
            }
            if (!booking.isCreated()) {
                throw new InvalidStateException(
                        "Booking " + bookingId + " cannot be confirmed from status " + booking.getBookingStatus());
            }

            bookingService.confirmBooking(booking, user);
            return paymentRepository.save(new Payment(
                    UUID.randomUUID().toString(), bookingId, user, PaymentStatus.SUCCESS,
                    nextAttempt(bookingId), Instant.now()));
        }
    }

    public Payment paymentFailed(final String bookingId, final String user) {
        synchronized (monitorFor(bookingId)) {
            final Booking booking = bookingService.getBooking(bookingId);
            requireOwner(booking, user);

            if (!booking.isCreated()) {
                throw new InvalidStateException(
                        "Booking " + bookingId + " cannot receive a payment in status " + booking.getBookingStatus());
            }

            final int attempt = nextAttempt(bookingId);
            final Payment payment = paymentRepository.save(new Payment(
                    UUID.randomUUID().toString(), bookingId, user, PaymentStatus.FAILED,
                    attempt, Instant.now()));

            if (attempt > paymentProperties.allowedRetries()) {
                bookingService.expireBooking(booking);
            }
            return payment;
        }
    }

    public List<Payment> getPayments(final String bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    private int nextAttempt(final String bookingId) {
        return paymentRepository.findByBookingId(bookingId).size() + 1;
    }

    private void requireOwner(final Booking booking, final String user) {
        if (!booking.getUser().equals(user)) {
            throw new BadRequestException("Booking " + booking.getId() + " does not belong to user " + user);
        }
    }

    private Object monitorFor(final String bookingId) {
        return bookingMonitors.computeIfAbsent(bookingId, ignored -> new Object());
    }
}
