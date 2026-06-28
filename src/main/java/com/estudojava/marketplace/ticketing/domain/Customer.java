package com.estudojava.marketplace.ticketing.domain;

import lombok.Getter;
import org.springframework.util.Assert;

import java.util.UUID;

@Getter
public class Customer {
    private UUID id;
    private CustomerId  correlationId;
    private String name;

    public Customer(String correlationId,  String name) {
        Assert.notNull(name, "O campo 'name' deve ser informado");

        this.id = UUID.randomUUID();
        this.correlationId = new CustomerId(correlationId);
        this.name = name;
    }

}
