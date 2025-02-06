package io.playground.service;

import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CompanyService {
    CompanyOut create(CompanyIn company);

    @Transactional(readOnly = true)
    CompanyOut getById(Long id);

    @Transactional(readOnly = true)
    List<CompanyOut> getAll();

    void delete(Long id);

    @Transactional(readOnly = true)
    boolean exists(Long id);
}