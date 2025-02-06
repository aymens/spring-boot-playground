package io.playground.service;

import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface DepartmentService {
    DepartmentOut create(DepartmentIn department);

    @Transactional(readOnly = true)
    DepartmentOut getById(Long id);

    @Transactional(readOnly = true)
    List<DepartmentOut> getByCompanyId(Long companyId);

    void delete(Long id,
                @Nullable Long transferToDepartmentId);

    @Transactional(readOnly = true)
    boolean exists(Long id);

    @Transactional(readOnly = true)
    Page<DepartmentOut> find(Long companyId,
                             String nameFilter,
                             Integer minEmployees,
                             Pageable pageable);
}