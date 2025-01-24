package io.playground.service;

import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;

import java.util.List;

public interface EmployeeService {
    EmployeeOut create(EmployeeIn employee);

    EmployeeOut getById(Long id);

    List<EmployeeOut> getByDepartmentId(Long departmentId);

    void delete(Long id);

    boolean exists(Long id);
}