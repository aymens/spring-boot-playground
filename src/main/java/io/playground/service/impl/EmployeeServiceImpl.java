package io.playground.service.impl;

import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.mapper.EmployeeMapper;
import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.service.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public EmployeeOut create(EmployeeIn employeeIn) {
        Department department = departmentRepository.findById(employeeIn.getDepartmentId())
                .orElseThrow(() -> new BusinessException("Department not found: " + employeeIn.getDepartmentId()));

        if (employeeRepository.existsByEmail(employeeIn.getEmail())) {
            throw new BusinessException("Employee with email already exists: " + employeeIn.getEmail());
        }

        Employee employee = employeeMapper.map(employeeIn, department);
        employee = employeeRepository.save(employee);
        return employeeMapper.map(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeOut getById(Long id) {
        return employeeRepository.findById(id)
                .map(employeeMapper::map)
                .orElseThrow(() -> new BusinessException("Employee not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeOut> getByDepartmentId(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new BusinessException("Department not found: " + departmentId);
        }
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(employeeMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new BusinessException("Employee not found: " + id);
        }
        employeeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return employeeRepository.existsById(id);
    }
}