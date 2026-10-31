package com.uditagarwal.services;

import com.uditagarwal.exceptions.NotFoundException;
import com.uditagarwal.model.Movie;
import com.uditagarwal.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public Movie getMovie(final String movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException("Movie not found: " + movieId));
    }

    public Movie createMovie(final String movieName) {
        final Movie movie = new Movie(UUID.randomUUID().toString(), movieName);
        log.debug("Creating movie {}", movieName);
        return movieRepository.save(movie);
    }
}
