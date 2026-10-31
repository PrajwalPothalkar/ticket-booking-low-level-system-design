package com.uditagarwal.repository;

import com.uditagarwal.model.Screen;
import com.uditagarwal.model.Seat;
import com.uditagarwal.model.Theatre;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTheatreRepository implements TheatreRepository {

    private final Map<String, Theatre> theatres = new ConcurrentHashMap<>();
    private final Map<String, Screen> screens = new ConcurrentHashMap<>();
    private final Map<String, Seat> seats = new ConcurrentHashMap<>();

    @Override
    public Theatre saveTheatre(final Theatre theatre) {
        theatres.put(theatre.getId(), theatre);
        return theatre;
    }

    @Override
    public Optional<Theatre> findTheatreById(final String theatreId) {
        return Optional.ofNullable(theatres.get(theatreId));
    }

    @Override
    public Screen saveScreen(final Screen screen) {
        screens.put(screen.getId(), screen);
        return screen;
    }

    @Override
    public Optional<Screen> findScreenById(final String screenId) {
        return Optional.ofNullable(screens.get(screenId));
    }

    @Override
    public Seat saveSeat(final Seat seat) {
        seats.put(seat.getId(), seat);
        return seat;
    }

    @Override
    public Optional<Seat> findSeatById(final String seatId) {
        return Optional.ofNullable(seats.get(seatId));
    }
}
