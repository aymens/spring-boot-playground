package com.example.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentIn {
    @NotBlank(message = "Department name is required")
    @Size(max = 50, message = "Department name cannot exceed 50 characters")
    private String name;

    @NotNull(message = "Company ID is required")
    private Long companyId;
}