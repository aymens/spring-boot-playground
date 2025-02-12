package io.playground.it.repository;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.it.BaseCoreIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class DepartmentRepositoryIT extends BaseCoreIntegrationTest {

    @Test
    void findByCompanyIdAndEmployeeCountBetween_ReturnsCorrectDepartments() {
        // Create test company with departments and varying employee counts
        Company company = createTestCompany();

        Department dept1 = createTestDepartment(company);
        IntStream.range(0, 5).forEach(i -> createTestEmployee(dept1));

        Department dept2 = createTestDepartment(company);
        IntStream.range(0, 2).forEach(i -> createTestEmployee(dept2));

        Department dept3 = createTestDepartment(company);
        IntStream.range(0, 4).forEach(i -> createTestEmployee(dept3));

        // Test with min=3 and max=5
        Page<Department> result = departmentRepository.findByCompanyIdAndEmployeeCountBetween(
                company.getId(), 3, 5, Pageable.unpaged());

        assertThat(result)
                .hasSize(2)
                .extracting(Department::getId)
                .containsExactlyInAnyOrder(dept1.getId(), dept3.getId());
    }

    @Test
    void findDepartmentWithMostRecentHire_ReturnsCorrectDepartment() {
        // Create test company with departments and employees with different hire dates
        Company company = createTestCompany();

        Department dept1 = createTestDepartment(company);
        Employee oldestHire = createTestEmployee(dept1);
        oldestHire.setHireDate(Instant.now().minus(Duration.ofDays(30)));
        employeeRepository.save(oldestHire);

        Department dept2 = createTestDepartment(company);
        Employee mostRecentHire = createTestEmployee(dept2);
        mostRecentHire.setHireDate(Instant.now().minus(Duration.ofDays(1)));
        employeeRepository.save(mostRecentHire);

        // Test finding department with most recent hire
        Optional<Department> result = departmentRepository.findDepartmentWithMostRecentHire(company.getId());

        assertThat(result)
                .isPresent()
                .get()
                .extracting(Department::getId)
                .isEqualTo(dept2.getId());
    }
}
