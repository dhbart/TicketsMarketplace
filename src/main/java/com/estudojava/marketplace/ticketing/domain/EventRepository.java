package com.estudojava.marketplace.ticketing.domain;

import java.util.List;

public interface EventRepository {
    void save(Event event);
    boolean existsSeat(EventId eventId, SeatId seatId);
    boolean tryLockSeat(EventId eventId, SeatId seatId, CustomerId customerId);
}
