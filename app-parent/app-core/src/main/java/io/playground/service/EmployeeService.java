package io.playground.service;

import io.playground.domain.Employee;
import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;

import java.util.List;

public interface EmployeeService extends DomainService<Employee, Long, EmployeeIn, EmployeeOut> {
    List<EmployeeOut> getByDepartmentId(Long departmentId);
}