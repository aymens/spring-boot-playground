package io.playground.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyOut implements Dto {
    private Long id;
    private String name;
    private String taxId;
    private Instant createdAt;
}