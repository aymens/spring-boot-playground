package io.playground.service;

import io.playground.domain.Company;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.repository.CompanyRepository;

public interface CompanyService extends DomainService<Company, Long, CompanyRepository, CompanyIn, CompanyOut> {

}