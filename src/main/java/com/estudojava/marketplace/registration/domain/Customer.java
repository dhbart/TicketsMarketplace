package com.estudojava.marketplace.registration.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

@Getter
@Setter
public class Customer {
    private CustomerId id;
    private String name;
    private String email;

    public Customer(CustomerId id, String email, String name) {
        Assert.notNull(email, "O campo 'email' deve ser informado");
        Assert.notNull(name, "O campo 'name' deve ser informado");

        this.id = id;
        this.email = email;
        this.name = name;
    }

    public Customer(String name, String email) {
        this(new CustomerId(), name, email);
    }
}
