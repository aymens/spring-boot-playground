package io.playground.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentIn implements Dto {
    @NotBlank(message = "Department name is required")
    @Size(max = 50, message = "Department name cannot exceed 50 characters")
    private String name;

    @NotNull(message = "Company ID is required")
    private Long companyId;
}