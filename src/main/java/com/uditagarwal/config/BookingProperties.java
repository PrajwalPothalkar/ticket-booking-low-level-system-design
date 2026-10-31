package com.uditagarwal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "booking")
public record BookingProperties(
        @DefaultValue("600") int lockTimeoutSeconds,
        @DefaultValue("10") int maxSeatsPerBooking,
        @DefaultValue("30000") long expiryScanIntervalMs
) {
}
