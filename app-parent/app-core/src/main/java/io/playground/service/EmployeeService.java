package io.playground.service;

import io.playground.domain.Employee;
import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import io.playground.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;

public interface EmployeeService extends DomainService<Employee, Long, EmployeeRepository, EmployeeIn, EmployeeOut> {
    Page<EmployeeOut> getByDepartmentId(Long departmentId, Pageable pageable);
    Page<EmployeeOut> findByDepartmentIdHireDateMinSalary(Long departmentId,
                                                          Instant hireDate,
                                                          BigDecimal minSalary,
                                                          Pageable pageable);
}