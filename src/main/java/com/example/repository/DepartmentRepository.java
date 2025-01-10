package com.example.repository;

import com.example.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByCompanyId(Long companyId);
    boolean existsByNameAndCompanyId(String name, Long companyId);
}
