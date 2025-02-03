package io.playground.repository;

import io.playground.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByCompanyId(Long companyId);
    boolean existsByNameIgnoreCaseAndCompany_Id(String name, Long companyId);
}
