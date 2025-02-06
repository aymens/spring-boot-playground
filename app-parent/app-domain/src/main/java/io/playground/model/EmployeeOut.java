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
public class EmployeeOut implements Dto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Instant hireDate;
    private Long departmentId;
}