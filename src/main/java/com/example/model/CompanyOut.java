package com.example.model;

import lombok.Data;

import java.time.Instant;

@Data
public class CompanyOut {
    private Long id;
    private String name;
    private String taxId;
    private Instant createdAt;
}