package io.playground.repository;

import io.playground.domain.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByCompanyId(Long companyId);
    boolean existsByNameIgnoreCaseAndCompany_Id(String name, Long companyId);
//    @Query("""
//        SELECT d FROM Department d
//        WHERE d.company.id = :companyId
//        AND (SELECT COUNT(e) FROM Employee e WHERE e.department = d)
//        BETWEEN :minEmployees AND :maxEmployees
//        ORDER BY d.name ASC
//        """)
//    List<Department> findByCompanyIdAndEmployeeCountBetween(
//            @Param("companyId") Long companyId,
//            @Param("minEmployees") int minEmployees,
//            @Param("maxEmployees") int maxEmployees
//    );
//
//    @Query("""
//        SELECT d FROM Department d
//        WHERE d.company.id = :companyId
//        AND EXISTS (
//            SELECT 1 FROM Employee e
//            WHERE e.department = d
//        )
//        ORDER BY (
//            SELECT MAX(e.hireDate)
//            FROM Employee e
//            WHERE e.department = d
//        ) DESC
//        LIMIT 1
//        """)
//    Optional<Department> findDepartmentWithMostRecentHire(@Param("companyId") Long companyId);
    Page<Department> findAll(Specification<Department> spec, Pageable pageable);
}
