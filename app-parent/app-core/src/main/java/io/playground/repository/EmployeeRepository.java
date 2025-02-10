package io.playground.repository;

import io.playground.domain.Employee;

import java.time.Instant;
import java.util.List;

public interface EmployeeRepository extends BaseJpaRepository<Employee, Long> {
    List<Employee> findByDepartmentId(Long departmentId);
    boolean existsByEmail(String email);
////    @Query("SELECT e FROM Employee e WHERE e.hireDate >= :since")
//    List<Employee> findByHireDateGreaterThanEqual(/*@Param("since") */Instant since);
    //TODO do something with these
    List<Employee> findByHireDateGreaterThanEqual(Instant since);
    List<Employee> findByDepartmentCompanyId(Long companyId);
    List<Employee> findByLastNameContainingIgnoreCase(String prefix);

}
