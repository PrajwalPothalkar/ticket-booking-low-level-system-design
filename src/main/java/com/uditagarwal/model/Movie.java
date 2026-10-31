package com.uditagarwal.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode(of = "id")
public class Movie {

    private final String id;
    private final String name;

    public Movie(@NonNull final String id, @NonNull final String name) {
        this.id = id;
        this.name = name;
    }
}
