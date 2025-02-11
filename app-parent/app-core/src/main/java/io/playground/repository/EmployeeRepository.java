package io.playground.repository;

import io.playground.domain.Employee;
import io.playground.repository.spec.EmployeeSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public interface EmployeeRepository extends BaseJpaRepository<Employee, Long> {
    List<Employee> findByDepartmentId(Long departmentId);

    boolean existsByEmail(String email);

    //TODO do something with these: service; controller; test
    List<Employee> findByHireDateIsGreaterThanEqual(Instant since);

    List<Employee> findByDepartmentCompanyId(Long companyId);

    List<Employee> findByLastNameContainingIgnoreCase(String prefix);

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
