package com.estudojava.marketplace.ticketing.domain;

public class SeatAlreadyReservedException extends RuntimeException {

    public SeatAlreadyReservedException() {
        super("Seat already reserved");
    }
}