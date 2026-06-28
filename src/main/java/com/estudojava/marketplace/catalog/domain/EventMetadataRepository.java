package com.estudojava.marketplace.catalog.domain;

import java.util.Optional;

public interface EventMetadataRepository {
    Optional<EventMetadata> findByEventId(EventId eventId);
}
