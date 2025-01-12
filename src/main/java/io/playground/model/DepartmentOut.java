package io.playground.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentOut {
    private Long id;
    private String name;
    private Long companyId;
}