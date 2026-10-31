package com.uditagarwal.services;

import com.uditagarwal.exceptions.NotFoundException;
import com.uditagarwal.exceptions.ScreenAlreadyOccupiedException;
import com.uditagarwal.model.Movie;
import com.uditagarwal.model.Screen;
import com.uditagarwal.model.Show;
import com.uditagarwal.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;

    public Show getShow(final String showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found: " + showId));
    }

    public Show createShow(final Movie movie, final Screen screen, final Instant startTime,
                           final int durationInSeconds) {
        if (!checkIfShowCreationAllowed(screen, startTime, durationInSeconds)) {
            throw new ScreenAlreadyOccupiedException(
                    "Screen " + screen.getId() + " is already occupied during the requested slot");
        }
        final Show show = new Show(UUID.randomUUID().toString(), movie, screen, startTime, durationInSeconds);
        return showRepository.save(show);
    }

    public boolean checkIfShowCreationAllowed(final Screen screen, final Instant startTime,
                                              final int durationInSeconds) {
        return showRepository.findByScreen(screen).stream()
                .noneMatch(existing -> existing.overlaps(startTime, durationInSeconds));
    }
}
