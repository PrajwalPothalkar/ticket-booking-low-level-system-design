package com.uditagarwal.repository;

import com.uditagarwal.model.Movie;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMovieRepository implements MovieRepository {

    private final Map<String, Movie> movies = new ConcurrentHashMap<>();

    @Override
    public Movie save(final Movie movie) {
        movies.put(movie.getId(), movie);
        return movie;
    }

    @Override
    public Optional<Movie> findById(final String movieId) {
        return Optional.ofNullable(movies.get(movieId));
    }
}
