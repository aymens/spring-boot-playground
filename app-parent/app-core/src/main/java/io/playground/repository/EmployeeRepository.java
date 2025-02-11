package io.playground.repository;

import io.playground.domain.Employee;
import io.playground.repository.spec.EmployeeSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface EmployeeRepository extends BaseJpaRepository<Employee, Long> {
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    boolean existsByEmail(String email);

    //TODO do something with these: service; controller; test
    Page<Employee> findByHireDateIsGreaterThanEqual(Instant since, Pageable pageable);

    Page<Employee> findByDepartmentCompanyId(Long companyId, Pageable pageable);

    Page<Employee> findByLastNameContainingIgnoreCase(String prefix, Pageable pageable);

    /**
     * Retrieves a paginated list of employees associated with the specified company ID.
     * This method uses a JPA Specification to filter employees by their company's
     * identifier.
     *
     * @param companyId the ID of the company to filter employees by; if null,
     *                  returns a page of all employees
     * @param pageable  pagination and sorting parameters
     * @return a page of employees matching the criteria. The page size and number
     * are determined by the pageable parameter
     */
    default Page<Employee> findByCompanyId(Long companyId, Pageable pageable) {
        return findAll(EmployeeSpecs.inCompany(companyId), pageable);
    }
}
