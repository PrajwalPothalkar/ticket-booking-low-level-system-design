package com.uditagarwal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
        @DefaultValue("2") int allowedRetries
) {
}
