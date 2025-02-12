package io.playground.it;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.test.it.BaseIntegrationTest_Pg16;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static io.playground.helper.NumberUtils.randomBigDecimal;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BaseCoreIntegrationTest extends BaseIntegrationTest_Pg16 {
    @Autowired
    protected CompanyRepository companyRepository;
    @Autowired
    protected DepartmentRepository departmentRepository;
    @Autowired
    protected EmployeeRepository employeeRepository;
    @Autowired
    protected Faker faker;
    protected Company testCompany;

    /**
     * Overriding child classes methods must be annotation with @{@link BeforeEach} to kick in.<br/>
     * Or, do not override this method! Instead, add your own @BeforeEach method with a different name.
     */
    @BeforeEach
    protected void init() {
        cleanup();
        testCompany = createTestCompany();
    }

    @AfterEach
    protected void tearDown() {
        cleanup();
    }

    protected void cleanup() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        companyRepository.deleteAll();
    }

    protected Company createTestCompany() {
        return companyRepository.save(
                Company.builder()
                        .name(faker.company().name())
                        .taxId(faker.numerify("##########"))
                        .build());
    }

    protected Department createTestDepartment(Company company) {
        return departmentRepository.save(
                Department.builder()
                        .name(faker.commerce().department())
                        .company(company)
                        .build());
    }

    protected List<Department> createTestDepartments(Company company, int count) {
        assertThat(count).isPositive();
        return departmentRepository.saveAll(
                IntStream.range(0, count)
                        .mapToObj(_ -> Department
                                .builder()
                                .name(faker.commerce().department())
                                .company(company)
                                .build()
                        ).toList()
        );
    }

    protected Employee createTestEmployee(Department department) {
        return employeeRepository.save(Employee
                .builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(department)
                .hireDate(Instant.now())
                .salary(randomBigDecimal())
                .build());
    }
}