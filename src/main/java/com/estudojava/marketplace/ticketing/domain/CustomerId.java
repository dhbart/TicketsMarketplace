package com.estudojava.marketplace.ticketing.domain;

import org.springframework.util.Assert;

import java.util.UUID;

public record CustomerId(UUID id) {
    public CustomerId {
        Assert.notNull(id, "Id must not be null");
    }

    public CustomerId(String id) {
        this(UUID.fromString(id));
    }
}
