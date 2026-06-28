package com.estudojava.marketplace.ticketing.domain;

import com.estudojava.marketplace.catalog.domain.EventMetadata;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class Event {
    private UUID id;
    private EventId correlationId;
    private Map<Sector, List<Seat>> seats;

    public Event(String correlationId, Map<Sector, List<Seat>> seats) {
        this.id = UUID.randomUUID();
        this.correlationId = new EventId(correlationId);
        this.seats = seats;
    }
}
