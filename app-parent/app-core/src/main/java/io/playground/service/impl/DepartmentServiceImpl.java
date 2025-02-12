package io.playground.service.impl;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.mapper.DepartmentMapper;
import io.playground.mapper.Mapper;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static io.playground.repository.spec.DepartmentSpecs.*;

@Transactional
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends BaseDomainServiceImpl<Department, Long, DepartmentRepository, DepartmentIn,
        DepartmentOut> implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentMapper departmentMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    public DepartmentRepository getRepository() {
        return departmentRepository;
    }

    @Override
    public Mapper<Department, DepartmentIn, DepartmentOut> getMapper() {
        return departmentMapper;
    }

    @Override
    public DepartmentOut create(DepartmentIn departmentIn) {
        Company company = companyRepository.findById(departmentIn.getCompanyId())
                .orElseThrow(() -> NotFoundException.of(Company.class.getName(), departmentIn.getCompanyId()));

        if (departmentRepository.existsByNameIgnoreCaseAndCompany_Id(
                departmentIn.getName(), departmentIn.getCompanyId())) {
            //TODO DEL Sort.sort(Employee.class).by(Employee::getDepartment).ascending();
            throw new BusinessException("Department already exists in company: " + departmentIn.getName());
        }

        Department department = departmentMapper.map(departmentIn, company);
        department = departmentRepository.save(department);
        return departmentMapper.map(department);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DepartmentOut> getByCompanyId(Long companyId, Pageable pageable) {
        if (!companyRepository.existsById(companyId)) {
            throw NotFoundException.of(Company.class.getName(), companyId);
        }
        return departmentRepository.findByCompanyId(companyId, pageable).map(departmentMapper::map);
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

    @Transactional(readOnly = true)
    @Override
    public Page<DepartmentOut> find(
            Long companyId,
            String nameFilter,
            Integer minEmployees,
            Pageable pageable
    ) {
        Specification<Department> spec =
                Specification.where(hasCompanyId(companyId))
                        .and(nameContains(nameFilter))
                        .and(hasMinEmployees(minEmployees));

        return departmentRepository.findAll(spec, pageable)
                .map(departmentMapper::map);
    }
    //TODO test
    @Override
    public Page<DepartmentOut> findByCompanyIdAndEmployeeCountBetween(Long companyId,
                                                                      int minEmployees,
                                                                      int maxEmployees,
                                                                      Pageable pageable) {
        return departmentRepository.findByCompanyIdAndEmployeeCountBetween(
                        companyId,
                        minEmployees,
                        maxEmployees,
                        pageable)
                .map(departmentMapper::map);
    }
    //TODO test
    @Override
    public Optional<DepartmentOut> findDepartmentWithMostRecentHire(Long companyId) {
        return departmentRepository.findDepartmentWithMostRecentHire(companyId)
                .map(departmentMapper::map);
    }
}