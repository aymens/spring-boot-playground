package com.example.service;

import com.example.model.EmployeeIn;
import com.example.model.EmployeeOut;

import java.util.List;

public interface EmployeeService {
    EmployeeOut create(EmployeeIn employee);

    EmployeeOut getById(Long id);

    List<EmployeeOut> getByDepartmentId(Long departmentId);

    void delete(Long id);

    boolean exists(Long id);
}