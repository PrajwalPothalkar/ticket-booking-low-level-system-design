package com.uditagarwal.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@EqualsAndHashCode(of = "id")
public class Theatre {

    private final String id;
    private final String name;
    private final List<Screen> screens = new ArrayList<>();

    public Theatre(@NonNull final String id, @NonNull final String name) {
        this.id = id;
        this.name = name;
    }

    public List<Screen> getScreens() {
        return Collections.unmodifiableList(screens);
    }

    public void addScreen(@NonNull final Screen screen) {
        screens.add(screen);
    }
}
