package com.uditagarwal.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

@Getter
@EqualsAndHashCode(of = "id")
public class Show {

    private final String id;
    private final Movie movie;
    private final Screen screen;
    private final Instant startTime;
    private final int durationInSeconds;

    public Show(@NonNull final String id, @NonNull final Movie movie, @NonNull final Screen screen,
                @NonNull final Instant startTime, final int durationInSeconds) {
        this.id = id;
        this.movie = movie;
        this.screen = screen;
        this.startTime = startTime;
        this.durationInSeconds = durationInSeconds;
    }

    public Instant getEndTime() {
        return startTime.plusSeconds(durationInSeconds);
    }

    /**
     * Two shows overlap when each starts before the other ends.
     */
    public boolean overlaps(final Instant otherStart, final int otherDurationInSeconds) {
        final Instant otherEnd = otherStart.plusSeconds(otherDurationInSeconds);
        return startTime.isBefore(otherEnd) && otherStart.isBefore(getEndTime());
    }
}
