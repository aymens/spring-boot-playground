package io.playground.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentIn {
    @NotBlank(message = "Department name is required")
    @Size(max = 50, message = "Department name cannot exceed 50 characters")
    private String name;

    @NotNull(message = "Company ID is required")
    private Long companyId;
}