package io.playground.service.impl;

import io.playground.domain.Company;
import io.playground.exception.BusinessException;
import io.playground.mapper.CompanyMapper;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.repository.CompanyRepository;
import io.playground.service.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new BusinessException("Company not found: " + id));
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
        if (!companyRepository.existsById(id)) {
            throw new BusinessException("Company not found: " + id);
        }
        companyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long id) {
        return companyRepository.existsById(id);
    }
}
