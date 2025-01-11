package io.playground.service;

import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;

import java.util.List;

public interface CompanyService {
    CompanyOut create(CompanyIn company);

    CompanyOut getById(Long id);

    List<CompanyOut> getAll();

    void delete(Long id);

    boolean exists(Long id);
}