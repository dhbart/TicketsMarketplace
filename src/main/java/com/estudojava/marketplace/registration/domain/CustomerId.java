package com.estudojava.marketplace.registration.domain;

import org.springframework.util.Assert;

import java.util.UUID;

public record CustomerId(UUID id) {
    public CustomerId {
        Assert.notNull(id, "Id must not be null");
    }

    public CustomerId(){
        this(UUID.randomUUID());
    }
}
