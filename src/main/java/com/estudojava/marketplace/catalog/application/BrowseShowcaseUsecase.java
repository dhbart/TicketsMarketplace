package com.estudojava.marketplace.catalog.application;

import com.estudojava.marketplace.catalog.application.dto.EventOutput;
import com.estudojava.marketplace.catalog.domain.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class BrowseShowcaseUsecase {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseShowcaseUsecase.class);

    private final EventRepository eventRepository;
    private final EventEnricher eventEnricher;

    public BrowseShowcaseUsecase(EventRepository eventRepository, EventEnricher eventEnricher) {
        this.eventRepository = eventRepository;
        this.eventEnricher = eventEnricher;
    }

    @Cacheable(value = "showcase", unless = "#result.isEmpty()")
    public List<EventOutput> execute() {
        //var events = eventRepository.findAll().stream().map(eventEnricher::enrich).toList();
        var futures = eventRepository.findAll().stream().map(eventEnricher::enrich).toList();

        var events = futures.stream()
                .map(CompletableFuture::join)
                .map(EventOutput::from).toList();

        LOGGER.info("{} events enriched", events.size());

        return events;
    }


}
