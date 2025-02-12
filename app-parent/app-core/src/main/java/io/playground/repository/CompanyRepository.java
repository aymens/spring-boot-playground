package io.playground.repository;

import io.playground.domain.Company;
import io.playground.model.CompanyOut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import static io.playground.repository.spec.CompanySpecs.withMinDepartments;
import static io.playground.repository.spec.CompanySpecs.withMinEmployees;
import static org.springframework.data.jpa.domain.Specification.where;

public interface CompanyRepository extends BaseJpaRepository<Company, Long> {
    boolean existsByTaxId(String taxId);

    //TODO do something with these
    @Query("SELECT c FROM Company c WHERE SIZE(c.departments) >= :minDepartments")
    Page<Company> findCompaniesWithMinDepartments(@Param("minDepartments") int minDepartments, Pageable pageable);

    /**
     * Finds a paginated list of companies that have at least the specified minimum number of employees.
     * The query is executed using JPQL to count employees associated with each company via their departments.
     *
     * @param minEmployees the minimum number of employees a company must have to be included in the result
     * @param pageable     the pagination and sorting information
     * @return a paginated list of companies that meet the criteria
     */
    @Query("SELECT c FROM Company c WHERE " +
            "(SELECT COUNT(e) FROM Employee e WHERE e.department.company = c) >= :minEmployees")
    Page<Company> findCompaniesWithMinEmployeesJPQL(@Param("minEmployees") int minEmployees, Pageable pageable);

    /**
     * Retrieves a paginated list of companies that have at least the specified minimum number of employees.
     * The search is performed using a JPA Specification for flexible filtering.
     *
     * @param minEmployees the minimum number of employees a company must have to be included in the result;
     *                     if null, no filtering is applied based on the number of employees
     * @param pageable     the pagination and sorting details
     * @return a paginated list of companies that satisfy the minimum employee criteria
     *///TODO can't this be replaced by the following, giving null for deps ?
    default Page<Company> findCompaniesWithMinEmployeesSpec(Integer minEmployees, Pageable pageable) {
        return findAll(withMinEmployees(minEmployees), pageable);
    }

    /**
     * Finds a paginated list of companies that have at least the specified minimum number of departments
     * and employees. The search uses specifications to filter companies based on the given criteria.
     *
     * @param minDepartments the minimum number of departments a company must have to be included in the result
     * @param minEmployees   the minimum number of employees a company must have to be included in the result
     * @param pageable       the pagination and sorting information
     * @return a paginated list of companies that meet the specified criteria
     */
    //TODO test; service; endpoint
    default Page<Company> findCompaniesWithMinDepartmentsAndEmployees(int minDepartments,
                                                                      int minEmployees,
                                                                      Pageable pageable) {
        return findAll(
                where(withMinDepartments(minDepartments))
                        .and(withMinEmployees(minEmployees)),
                pageable);
    }
}
