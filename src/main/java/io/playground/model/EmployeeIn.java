package io.playground.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class EmployeeIn {
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private Instant hireDate;
}