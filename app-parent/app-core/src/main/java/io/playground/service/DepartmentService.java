package io.playground.service;

import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import org.springframework.lang.Nullable;

import java.util.List;

public interface DepartmentService {
    DepartmentOut create(DepartmentIn department);

    DepartmentOut getById(Long id);

    List<DepartmentOut> getByCompanyId(Long companyId);

    void delete(Long id, @Nullable Long transferToDepartmentId);

    boolean exists(Long id);
}