package com.estudojava.marketplace.ticketing.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    @Id
    private UUID id;

    private UUID correlationId;

    @NotBlank
    @Column(nullable = false)
    private String name;
}

