package io.playground.repository;

import io.playground.domain.Company;
import io.playground.repository.spec.CompanySpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * @param pageable the pagination and sorting information
     * @return a paginated list of companies that meet the criteria
     */
    @Query("SELECT c FROM Company c WHERE " +
            "(SELECT COUNT(e) FROM Employee e WHERE e.department.company = c) >= :minEmployees")
    Page<Company> findCompaniesWithMinEmployeesJPQL(@Param("minEmployees") int minEmployees, Pageable pageable);

    /**
     * Finds and retrieves a list of companies that have at least the specified minimum number of employees.
     * The method utilizes a specification to query the database.
     *
     * @param minEmployees the minimum number of employees a company must have to be included in the result;
     *                     if null, no filter on employee count is applied
     * @return a list of companies that meet the specified minimum employee count criteria
     */
    default Page<Company> findCompaniesWithMinEmployeesSpec(Integer minEmployees, Pageable pageable) {
        return findAll(CompanySpecs.withMinEmployees(minEmployees), pageable);
    }
}
