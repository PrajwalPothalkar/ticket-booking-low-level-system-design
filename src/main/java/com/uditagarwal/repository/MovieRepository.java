package com.uditagarwal.repository;

import com.uditagarwal.model.Movie;

import java.util.Optional;

public interface MovieRepository {

    Movie save(Movie movie);

    Optional<Movie> findById(String movieId);
}
