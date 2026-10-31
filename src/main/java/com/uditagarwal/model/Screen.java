package com.uditagarwal.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode(of = "id")
public class Screen {

    private final String id;
    private final String name;
    private final Theatre theatre;
    private final List<Seat> seats = new ArrayList<>();

    public Screen(@NonNull final String id, @NonNull final String name, @NonNull final Theatre theatre) {
        this.id = id;
        this.name = name;
        this.theatre = theatre;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public void addSeat(@NonNull final Seat seat) {
        this.seats.add(seat);
    }
}
