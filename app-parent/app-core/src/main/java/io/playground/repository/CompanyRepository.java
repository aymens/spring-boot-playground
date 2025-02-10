package io.playground.repository;

import io.playground.domain.Company;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends BaseJpaRepository<Company, Long> {
    boolean existsByTaxId(String taxId);
    @Query("SELECT c FROM Company c WHERE SIZE(c.departments) >= :minDepartments")
    List<Company> findCompaniesWithMinDepartments(@Param("minDepartments") int minDepartments);
    @Query("SELECT c FROM Company c WHERE " +
            "(SELECT COUNT(e) FROM Employee e WHERE e.department.company = c) >= :minEmployees")
    List<Company> findCompaniesWithMinEmployees(@Param("minEmployees") int minEmployees);

}
