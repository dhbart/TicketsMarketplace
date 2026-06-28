package com.estudojava.marketplace.registration.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@RequiredArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    String street, city, state, postalCode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdOn;


}
