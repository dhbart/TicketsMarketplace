package com.estudojava.marketplace.catalog.application;

import com.estudojava.marketplace.catalog.domain.Event;
import com.estudojava.marketplace.catalog.domain.EventMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventEnricher {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseShowcaseUsecase.class);

    private final EventMetadataRepository eventMetadataRepository;

    public EventEnricher(EventMetadataRepository eventMetadataRepository) {
        this.eventMetadataRepository = eventMetadataRepository;
    }

    @Async
    public CompletableFuture<Event> enrich(Event event) {
    //public Event enrich(Event event) {

        LOGGER.info("Enriching event: {}", event);

        var metadata = eventMetadataRepository.findByEventId(event.getId());
        event.setMetadata(metadata);

        return CompletableFuture.completedFuture(event);
    }
}
