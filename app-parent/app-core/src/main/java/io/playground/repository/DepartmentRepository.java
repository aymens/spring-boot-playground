package io.playground.repository;

import io.playground.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DepartmentRepository extends BaseJpaRepository<Department, Long> {
    Page<Department> findByCompanyId(Long companyId, Pageable pageable);
    boolean existsByNameIgnoreCaseAndCompany_Id(String name, Long companyId);
    //TODO do something with these
    @Query("""
        SELECT d FROM Department d
        WHERE d.company.id = :companyId
        AND (SELECT COUNT(e) FROM Employee e WHERE e.department = d)
        BETWEEN :minEmployees AND :maxEmployees
        ORDER BY d.name ASC
        """)
    Page<Department> findByCompanyIdAndEmployeeCountBetween(
            @Param("companyId") Long companyId,
            @Param("minEmployees") int minEmployees,
            @Param("maxEmployees") int maxEmployees,
            Pageable pageable
    );

    @Query("""
        SELECT d FROM Department d
        WHERE d.company.id = :companyId
        AND EXISTS (
            SELECT 1 FROM Employee e
            WHERE e.department = d
        )
        ORDER BY (
            SELECT MAX(e.hireDate)
            FROM Employee e
            WHERE e.department = d
        ) DESC
        LIMIT 1
        """)
    Optional<Department> findDepartmentWithMostRecentHire(@Param("companyId") Long companyId);
}
