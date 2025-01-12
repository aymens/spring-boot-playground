package io.playground.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class EmployeeOut {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Instant hireDate;
    private Long departmentId;
}