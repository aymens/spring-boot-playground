package io.playground.service;

import io.playground.domain.Department;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.util.List;

public interface DepartmentService extends DomainService<Department, Long, DepartmentIn, DepartmentOut> {

    List<DepartmentOut> getByCompanyId(Long companyId);

    void delete(Long id,
                @Nullable Long transferToDepartmentId);

    Page<DepartmentOut> find(Long companyId,
                             String nameFilter,
                             Integer minEmployees,
                             Pageable pageable);
}