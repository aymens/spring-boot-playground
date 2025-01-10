package com.example.service;

import com.example.model.CompanyIn;
import com.example.model.CompanyOut;

import java.util.List;

public interface CompanyService {
    CompanyOut create(CompanyIn company);

    CompanyOut getById(Long id);

    List<CompanyOut> getAll();

    void delete(Long id);

    boolean exists(Long id);
}