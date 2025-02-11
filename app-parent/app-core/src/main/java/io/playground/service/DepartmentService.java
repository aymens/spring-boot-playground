package io.playground.service;

import io.playground.domain.Department;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.repository.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public interface DepartmentService extends DomainService<Department, Long, DepartmentRepository, DepartmentIn, DepartmentOut> {

    Page<DepartmentOut> getByCompanyId(Long companyId, Pageable pageable);

    void delete(Long id,
                @Nullable Long transferToDepartmentId);

    Page<DepartmentOut> find(Long companyId,
                             String nameFilter,
                             Integer minEmployees,
                             Pageable pageable);
}