package com.example.service;

import com.example.model.DepartmentIn;
import com.example.model.DepartmentOut;

import java.util.List;

public interface DepartmentService {
    DepartmentOut create(DepartmentIn department);

    DepartmentOut getById(Long id);

    List<DepartmentOut> getByCompanyId(Long companyId);

    void delete(Long id);

    boolean exists(Long id);
}