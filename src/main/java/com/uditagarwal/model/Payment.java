package com.uditagarwal.model;

import java.time.Instant;

/**
 * One row per payment attempt. Keeping attempts (instead of a single counter)
 * makes retries, auditing and idempotency trivial.
 */
public record Payment(
        String id,
        String bookingId,
        String userId,
        PaymentStatus status,
        int attempt,
        Instant createdAt
) {
}
