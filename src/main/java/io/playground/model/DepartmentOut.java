package io.playground.model;

import lombok.Data;

@Data
public class DepartmentOut {
    private Long id;
    private String name;
    private Long companyId;
}