package com.uditagarwal.repository;

import com.uditagarwal.model.Screen;
import com.uditagarwal.model.Show;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryShowRepository implements ShowRepository {

    private final Map<String, Show> shows = new ConcurrentHashMap<>();

    @Override
    public Show save(final Show show) {
        shows.put(show.getId(), show);
        return show;
    }

    @Override
    public Optional<Show> findById(final String showId) {
        return Optional.ofNullable(shows.get(showId));
    }

    @Override
    public List<Show> findByScreen(final Screen screen) {
        return shows.values().stream()
                .filter(show -> show.getScreen().equals(screen))
                .toList();
    }

    @Override
    public List<Show> findAll() {
        return List.copyOf(shows.values());
    }
}
