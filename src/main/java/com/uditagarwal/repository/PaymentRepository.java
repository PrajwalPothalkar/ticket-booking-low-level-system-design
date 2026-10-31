package com.uditagarwal.repository;

import com.uditagarwal.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    List<Payment> findByBookingId(String bookingId);

    Optional<Payment> findSuccessfulByBookingId(String bookingId);
}
