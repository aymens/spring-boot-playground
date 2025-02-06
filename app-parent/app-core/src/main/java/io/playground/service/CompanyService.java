package io.playground.service;

import io.playground.domain.Company;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;

public interface CompanyService extends DomainService<Company, Long, CompanyIn, CompanyOut> {

}