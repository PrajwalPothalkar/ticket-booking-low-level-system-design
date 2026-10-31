package com.uditagarwal.repository;

import com.uditagarwal.model.Screen;
import com.uditagarwal.model.Show;

import java.util.List;
import java.util.Optional;

public interface ShowRepository {

    Show save(Show show);

    Optional<Show> findById(String showId);

    List<Show> findByScreen(Screen screen);

    List<Show> findAll();
}
