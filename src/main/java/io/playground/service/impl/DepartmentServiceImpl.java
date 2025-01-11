package io.playground.service.impl;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.exception.BusinessException;
import io.playground.mapper.DepartmentMapper;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.service.DepartmentService;
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

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 CompanyRepository companyRepository,
                                 DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public DepartmentOut create(DepartmentIn departmentIn) {
        Company company = companyRepository.findById(departmentIn.getCompanyId())
                .orElseThrow(() -> new BusinessException("Company not found: " + departmentIn.getCompanyId()));

        if (departmentRepository.existsByNameAndCompanyId(
                departmentIn.getName(), departmentIn.getCompanyId())) {
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
                .orElseThrow(() -> new BusinessException("Department not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentOut> getByCompanyId(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new BusinessException("Company not found: " + companyId);
        }
        return departmentRepository.findByCompanyId(companyId).stream()
                .map(departmentMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new BusinessException("Department not found: " + id);
        }
        departmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return departmentRepository.existsById(id);
    }
}