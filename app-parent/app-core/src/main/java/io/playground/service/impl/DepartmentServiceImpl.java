package io.playground.service.impl;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.mapper.DepartmentMapper;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.service.DepartmentService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentMapper departmentMapper;
    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 CompanyRepository companyRepository,
                                 DepartmentMapper departmentMapper,
                                 EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.departmentMapper = departmentMapper;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public DepartmentOut create(DepartmentIn departmentIn) {
        Company company = companyRepository.findById(departmentIn.getCompanyId())
                .orElseThrow(() -> new BusinessException("Company not found: " + departmentIn.getCompanyId()));

        if (departmentRepository.existsByNameAndCompany_Id(
                departmentIn.getName(), departmentIn.getCompanyId())) {
            //TODO DEL Sort.sort(Employee.class).by(Employee::getDepartment).ascending();
            throw new BusinessException("Department already exists in company: " + departmentIn.getName());
        }

        Department department = departmentMapper.map(departmentIn, company);
        department = departmentRepository.save(department);
        return departmentMapper.map(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentOut getById(Long id) {
        return departmentRepository.findById(id)
                .map(departmentMapper::map)
                .orElseThrow(() -> NotFoundException.of(Department.class.getName(), id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentOut> getByCompanyId(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw NotFoundException.of(Company.class.getName(), companyId);
        }
        return departmentRepository.findByCompanyId(companyId).stream()
                .map(departmentMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id, @Nullable Long transferToDepartmentId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of(Department.class.getName(), id));

        if (!department.getEmployees().isEmpty()) {
            if (transferToDepartmentId == null) {
                throw new BusinessException(
                        "Department has employees. Must specify a transfer department ID.");
            }

            Department targetDepartment = departmentRepository.findById(transferToDepartmentId)
                    .orElseThrow(() -> NotFoundException.of(Department.class.getName(), transferToDepartmentId));

            // Validate target department is in same company
            if (!targetDepartment.getCompany().getId().equals(department.getCompany().getId())) {
                throw new BusinessException("Target department must be in the same company");
            }

            // Don't allow transfer to self
            if (targetDepartment.getId().equals(department.getId())) {
                throw new BusinessException("Cannot transfer employees to the same department");
            }

            department.getEmployees().forEach(employee -> employee.setDepartment(targetDepartment));
            employeeRepository.saveAll(department.getEmployees());

            // Clear employees from the department to prevent cascade delete
            department.getEmployees().clear();
            departmentRepository.save(department);
        }

        departmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return departmentRepository.existsById(id);
    }
}