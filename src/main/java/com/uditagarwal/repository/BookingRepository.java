package com.uditagarwal.repository;

import com.uditagarwal.model.Booking;
import com.uditagarwal.model.Show;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(String bookingId);

    List<Booking> findByShow(Show show);

    List<Booking> findAll();
}
