package io.playground.web.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.test.it.BaseMockMvcIntegrationTest_Pg16;
import net.datafaker.Faker;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

public class BaseControllerIntegrationTest_Pg16 extends BaseMockMvcIntegrationTest_Pg16 {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    DepartmentRepository departmentRepository;
    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    Faker faker;

    Company testCompany;
    Department testDepartment;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        companyRepository.deleteAll();

        testCompany = createTestCompany();
        testDepartment = createTestDepartment(testCompany);
    }

    Company createTestCompany() {
        return companyRepository.save(
                Company.builder()
                        .name(faker.company().name())
                        .taxId(faker.numerify("##########"))
                        .build());
    }

    Department createTestDepartment(Company company) {
        return departmentRepository.save(
                Department.builder()
                        .name(faker.commerce().department())
                        .company(company)
                        .build());
    }

    List<Department> createTestDepartments(Company company, int count) {
        AssertionsForClassTypes.assertThat(count).isPositive();
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

    Employee createTestEmployee(Department department) {
        return employeeRepository.save(Employee
                .builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(department)
                .hireDate(Instant.now())
                .build());
    }
}
