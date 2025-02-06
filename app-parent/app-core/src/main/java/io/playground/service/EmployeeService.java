package io.playground.service;

import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface EmployeeService {
    EmployeeOut create(EmployeeIn employee);

    @Transactional(readOnly = true)
    EmployeeOut getById(Long id);

    @Transactional(readOnly = true)
    List<EmployeeOut> getByDepartmentId(Long departmentId);

    void delete(Long id);

    @Transactional(readOnly = true)
    boolean exists(Long id);
}