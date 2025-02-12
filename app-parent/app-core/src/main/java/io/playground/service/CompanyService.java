package io.playground.service;

import io.playground.domain.Company;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyService extends DomainService<Company, Long, CompanyRepository, CompanyIn, CompanyOut> {
    Page<CompanyOut> findCompaniesWithMinDepartments(int minDepartments, Pageable pageable);
    Page<CompanyOut> findCompaniesWithMinEmployees(int minEmployees, Pageable pageable);
    Page<CompanyOut> findCompaniesWithMinDepartmentsAndEmployees(int minDepartments,
                                                                 int minEmployees,
                                                                 Pageable pageable);
}