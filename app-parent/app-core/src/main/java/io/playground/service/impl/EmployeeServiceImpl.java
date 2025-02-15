package io.playground.service.impl;

import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.mapper.EmployeeMapper;
import io.playground.mapper.Mapper;
import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.repository.spec.EmployeeSpecs;
import io.playground.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Transactional
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends BaseDomainServiceImpl<Employee, Long, EmployeeRepository, EmployeeIn,
        EmployeeOut> implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeRepository getRepository() {
        return employeeRepository;
    }

    @Override
    public Mapper<Employee, EmployeeIn, EmployeeOut> getMapper() {
        return employeeMapper;
    }

    @Override
    public EmployeeOut create(EmployeeIn employeeIn) {
        Department department = departmentRepository.findById(employeeIn.getDepartmentId())
                .orElseThrow(() -> NotFoundException.of(Department.class.getName(), employeeIn.getDepartmentId()));

        if (employeeRepository.existsByEmail(employeeIn.getEmail())) {
            throw new BusinessException("Employee with email already exists: " + employeeIn.getEmail());
        }

        Employee employee = employeeMapper.map(employeeIn, department);
        employee = employeeRepository.save(employee);
        return employeeMapper.map(employee);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<EmployeeOut> getByDepartmentId(Long departmentId, Pageable pageable) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new BusinessException("Department not found: " + departmentId);
        }
        return employeeRepository.findByDepartmentId(departmentId, pageable)
                .map(employeeMapper::map);
    }

    @Override
    public Page<EmployeeOut> findByDepartmentIdHireDateMinSalary(Long departmentId,
                                                                 Instant hireDate,
                                                                 BigDecimal minSalary,
                                                                 Pageable pageable) {
        return employeeRepository.findAll(
                Specification.where(EmployeeSpecs.inDepartment(departmentId)
                        .and(EmployeeSpecs.hiredSince(hireDate)
                                .and(EmployeeSpecs.withMinSalary(minSalary)))),
                pageable)
                .map(employeeMapper::map);
    }
}