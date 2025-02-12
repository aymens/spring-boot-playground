package io.playground.it.repository;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.it.BaseCoreIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class CompanyRepositoryIT extends BaseCoreIntegrationTest {

    @Test
    void findCompaniesWithMinDepartments_ReturnsCorrectCompanies() {
        // Create test companies with varying numbers of departments
        Company company1 = createTestCompany();
        createTestDepartments(company1, 5);

        Company company2 = createTestCompany();
        createTestDepartments(company2, 2);

        Company company3 = createTestCompany();
        createTestDepartments(company3, 4);

        // Test with minDepartments = 4
        Page<Company> result = companyRepository.findCompaniesWithMinDepartments(4, Pageable.unpaged());

        assertThat(result)
                .hasSize(2)
                .extracting(Company::getId)
                .containsExactlyInAnyOrder(company1.getId(), company3.getId());
    }

    @Test
    void findCompaniesWithMinEmployeesJPQL_ReturnsCorrectCompanies() {
        // Create test companies with departments and employees
        Company company1 = createTestCompany();
        Department dept1 = createTestDepartment(company1);
        IntStream.range(0, 5).forEach(i -> createTestEmployee(dept1));

        Company company2 = createTestCompany();
        Department dept2 = createTestDepartment(company2);
        IntStream.range(0, 2).forEach(i -> createTestEmployee(dept2));

        // Test with minEmployees = 4
        Page<Company> result = companyRepository.findCompaniesWithMinEmployeesJPQL(4, Pageable.unpaged());

        assertThat(result)
                .hasSize(1)
                .extracting(Company::getId)
                .containsExactly(company1.getId());
    }

    @Test
    void findCompaniesWithMinEmployeesSpec_ReturnsCorrectCompanies() {
        // Create test companies with departments and employees
        Company company1 = createTestCompany();
        Department dept1 = createTestDepartment(company1);
        IntStream.range(0, 5).forEach(i -> createTestEmployee(dept1));

        Company company2 = createTestCompany();
        Department dept2 = createTestDepartment(company2);
        IntStream.range(0, 2).forEach(i -> createTestEmployee(dept2));

        // Test with minEmployees = 4
        Page<Company> result = companyRepository.findCompaniesWithMinEmployeesSpec(4, Pageable.unpaged());

        assertThat(result)
                .hasSize(1)
                .extracting(Company::getId)
                .containsExactly(company1.getId());
    }
}
