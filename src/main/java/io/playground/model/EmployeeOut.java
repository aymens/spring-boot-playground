package io.playground.model;

import lombok.Data;

import java.time.Instant;

@Data
public class EmployeeOut {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Instant hireDate;
    private Long departmentId;
}