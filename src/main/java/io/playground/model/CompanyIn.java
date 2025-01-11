package io.playground.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyIn {
    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Tax ID is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Tax ID must be exactly 10 digits")
    private String taxId;
}