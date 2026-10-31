package com.uditagarwal.repository;

import com.uditagarwal.model.Payment;
import com.uditagarwal.model.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    @Override
    public Payment save(final Payment payment) {
        payments.put(payment.id(), payment);
        return payment;
    }

    @Override
    public List<Payment> findByBookingId(final String bookingId) {
        return payments.values().stream()
                .filter(payment -> payment.bookingId().equals(bookingId))
                .sorted((a, b) -> Integer.compare(a.attempt(), b.attempt()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Payment> findSuccessfulByBookingId(final String bookingId) {
        return payments.values().stream()
                .filter(payment -> payment.bookingId().equals(bookingId))
                .filter(payment -> payment.status() == PaymentStatus.SUCCESS)
                .findFirst();
    }
}
