package io.playground.service.impl;

import io.playground.domain.Company;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.mapper.CompanyMapper;
import io.playground.mapper.Mapper;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.repository.CompanyRepository;
import io.playground.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl extends BaseDomainServiceImpl<Company, Long, CompanyRepository, CompanyIn,
        CompanyOut> implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    public CompanyRepository getRepository() {
        return companyRepository;
    }

    @Override
    public Mapper<Company, CompanyIn, CompanyOut> getMapper() {
        return companyMapper;
    }

    @Override
    public CompanyOut create(CompanyIn companyIn) {
        if (companyRepository.existsByTaxId(companyIn.getTaxId())) {
            throw new BusinessException("Company with tax ID already exists: " + companyIn.getTaxId());
        }

        Company company = companyMapper.map(companyIn);
        company = companyRepository.save(company);
        return companyMapper.map(company);
    }

    @Override
    public void delete(Long id) {
        log.debug("Attempting to delete company with id: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of(Company.class.getName(), id));

        boolean hasEmployees = company.getDepartments().stream()
                .anyMatch(d -> !d.getEmployees().isEmpty());

        if (hasEmployees) {
            log.error("Cannot delete company {} as it has departments with employees", id);
            throw new BusinessException(
                    "Cannot delete company with existing employees. Transfer or remove employees first.");
        }

        log.info("Deleting company: {} ({})", company.getName(), id);
        companyRepository.deleteById(id);
        log.debug("Company deleted successfully: {}", id);
    }
//TODO test
    @Override
    public Page<CompanyOut> findCompaniesWithMinDepartments(int minDepartments, Pageable pageable) {
        return companyRepository.findCompaniesWithMinDepartments(minDepartments, pageable)
                .map(companyMapper::map);
    }
    //TODO test
    @Override
    public Page<CompanyOut> findCompaniesWithMinEmployees(int minEmployees, Pageable pageable) {
        return companyRepository.findCompaniesWithMinEmployeesSpec(minEmployees, pageable)
                .map(companyMapper::map);
    }

    @Override
    public Page<CompanyOut> findCompaniesWithMinDepartmentsAndEmployees(int minDepartments, int minEmployees, Pageable pageable) {
        return companyRepository.findCompaniesWithMinDepartmentsAndEmployees(minDepartments, minEmployees, pageable)
                .map(companyMapper::map);
    }
}
