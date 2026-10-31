package com.uditagarwal.repository;

import com.uditagarwal.model.Screen;
import com.uditagarwal.model.Seat;
import com.uditagarwal.model.Theatre;

import java.util.Optional;

public interface TheatreRepository {

    Theatre saveTheatre(Theatre theatre);

    Optional<Theatre> findTheatreById(String theatreId);

    Screen saveScreen(Screen screen);

    Optional<Screen> findScreenById(String screenId);

    Seat saveSeat(Seat seat);

    Optional<Seat> findSeatById(String seatId);
}
