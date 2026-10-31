package com.uditagarwal.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode(of = "id")
public class Seat {

    private final String id;
    private final int rowNo;
    private final int seatNo;

    public Seat(@NonNull final String id, final int rowNo, final int seatNo) {
        this.id = id;
        this.rowNo = rowNo;
        this.seatNo = seatNo;
    }
}
