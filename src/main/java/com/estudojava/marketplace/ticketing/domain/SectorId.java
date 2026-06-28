package com.estudojava.marketplace.ticketing.domain;

import org.springframework.util.Assert;

public record SectorId (String id){
    public SectorId {
        Assert.notNull(id, "Id must not be null");
    }
}