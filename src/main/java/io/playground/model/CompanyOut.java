package io.playground.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CompanyOut {
    private Long id;
    private String name;
    private String taxId;
    private Instant createdAt;
}