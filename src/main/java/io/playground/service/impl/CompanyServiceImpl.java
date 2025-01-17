package io.playground.service.impl;

import io.playground.domain.Company;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.mapper.CompanyMapper;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.repository.CompanyRepository;
import io.playground.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyServiceImpl(CompanyRepository companyRepository,
                              CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
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
    @Transactional(readOnly = true)
    public CompanyOut getById(Long id) {
        return companyRepository.findById(id)
                .map(companyMapper::map)
                .orElseThrow(() -> NotFoundException.of(Company.class.getName(), id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyOut> getAll() {
        return companyRepository.findAll().stream()
                .map(companyMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.debug("Attempting to delete company with id: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Company not found for deletion: {}", id);
                    return new BusinessException("Company not found: " + id);
                });

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

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return companyRepository.existsById(id);
    }
}
