package com.uditagarwal.services;

import com.uditagarwal.exceptions.AlreadyExistsException;
import com.uditagarwal.exceptions.NotFoundException;
import com.uditagarwal.model.Screen;
import com.uditagarwal.model.Seat;
import com.uditagarwal.model.Theatre;
import com.uditagarwal.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public Theatre getTheatre(final String theatreId) {
        return theatreRepository.findTheatreById(theatreId)
                .orElseThrow(() -> new NotFoundException("Theatre not found: " + theatreId));
    }

    public Screen getScreen(final String screenId) {
        return theatreRepository.findScreenById(screenId)
                .orElseThrow(() -> new NotFoundException("Screen not found: " + screenId));
    }

    public Seat getSeat(final String seatId) {
        return theatreRepository.findSeatById(seatId)
                .orElseThrow(() -> new NotFoundException("Seat not found: " + seatId));
    }

    public Theatre createTheatre(final String theatreName) {
        return theatreRepository.saveTheatre(new Theatre(UUID.randomUUID().toString(), theatreName));
    }

    public Screen createScreenInTheatre(final String screenName, final Theatre theatre) {
        final Screen screen = new Screen(UUID.randomUUID().toString(), screenName, theatre);
        theatre.addScreen(screen);
        return theatreRepository.saveScreen(screen);
    }

    public Seat createSeatInScreen(final int rowNo, final int seatNo, final Screen screen) {
        final boolean seatExists = screen.getSeats().stream()
                .anyMatch(seat -> seat.getRowNo() == rowNo && seat.getSeatNo() == seatNo);
        if (seatExists) {
            throw new AlreadyExistsException(
                    "Seat already exists at row " + rowNo + ", seat " + seatNo);
        }
        final Seat seat = new Seat(UUID.randomUUID().toString(), rowNo, seatNo);
        screen.addSeat(seat);
        return theatreRepository.saveSeat(seat);
    }
}
