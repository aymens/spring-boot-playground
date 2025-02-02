package io.playground.service;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.it.BaseIntegrationTest_Pg16;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class CompanyServiceIT extends BaseIntegrationTest_Pg16 {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void create_WithValidInput_Succeeds() {
        CompanyIn input = CompanyIn.builder()
                .name("Test Company")
                .taxId("1234567890")
                .build();

        CompanyOut result = companyService.create(input);

        assertThat(result)
                .isNotNull()
                .satisfies(company -> {
                    assertThat(company.getId()).isNotNull();
                    assertThat(company.getName()).isEqualTo(input.getName());
                    assertThat(company.getTaxId()).isEqualTo(input.getTaxId());
                    assertThat(company.getCreatedAt()).isNotNull();
                });

        Company savedCompany = companyRepository.findById(result.getId()).orElseThrow();
        assertThat(savedCompany.getName()).isEqualTo(input.getName());
        assertThat(savedCompany.getTaxId()).isEqualTo(input.getTaxId());
    }

    @Test
    void create_WithDuplicateTaxId_ThrowsException() {
        CompanyIn first = CompanyIn.builder()
                .name("First Company")
                .taxId("1234567890")
                .build();
        companyService.create(first);

        CompanyIn second = CompanyIn.builder()
                .name("Second Company")
                .taxId("1234567890")
                .build();

        assertThatThrownBy(() -> companyService.create(second))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Company with tax ID already exists");
    }

    @Test
    void getById_WhenExists_ReturnsCompany() {
        CompanyIn input = CompanyIn.builder()
                .name("Test Company")
                .taxId("1234567890")
                .build();
        CompanyOut created = companyService.create(input);

        CompanyOut result = companyService.getById(created.getId());

        assertThat(result)
                .isNotNull()
                .satisfies(company -> {
                    assertThat(company.getId()).isEqualTo(created.getId());
                    assertThat(company.getName()).isEqualTo(created.getName());
                    assertThat(company.getTaxId()).isEqualTo(created.getTaxId());
                });
    }

    @Test
    void getById_WhenNotExists_ThrowsException() {
        // Create and delete a company to get a valid but non-existent ID
        CompanyOut created = companyService.create(CompanyIn.builder()
                .name("Temporary")
                .taxId("1234567890")
                .build());
        Long deletedId = created.getId();
        companyRepository.deleteById(deletedId);

        assertThatThrownBy(() -> companyService.getById(deletedId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company(" + deletedId + ") not found");
    }

    @Test
    void getAll_ReturnsAllCompanies() {
        CompanyIn company1 = CompanyIn.builder()
                .name("First Company")
                .taxId("1111111111")
                .build();
        CompanyIn company2 = CompanyIn.builder()
                .name("Second Company")
                .taxId("2222222222")
                .build();

        CompanyOut created1 = companyService.create(company1);
        CompanyOut created2 = companyService.create(company2);

        List<CompanyOut> results = companyService.getAll();

        assertThat(results)
                .hasSize(2)
                .extracting(CompanyOut::getId)
                .containsExactlyInAnyOrder(created1.getId(), created2.getId());

        assertThat(results)
                .extracting(CompanyOut::getName)
                .containsExactlyInAnyOrder("First Company", "Second Company");
    }

    @Test
    void delete_WhenExistsAndNoEmployees_DeletesCompanyAndDepartments() {
        Company company = companyRepository.save(Company.builder()
                .name("Test Company")
                .taxId("1234567890")
                .build());

        List<Department> departments = departmentRepository.saveAll(List.of(
                Department.builder().name("IT").company(company).build(),
                Department.builder().name("HR").company(company).build(),
                Department.builder().name("Finance").company(company).build()
        ));

        List<Long> departmentIds = departments.stream()
                .map(Department::getId)
                .toList();

        companyService.delete(company.getId());

        assertThat(companyRepository.existsById(company.getId())).isFalse();
        assertThat(departmentRepository.findAllById(departmentIds)).isEmpty();
    }

    @Test
    void delete_WithEmployees_ThrowsException() {
        Company company = companyRepository.save(Company.builder()
                .name("Test Company")
                .taxId("1234567890")
                .build());

        Department department = departmentRepository.save(Department.builder()
                .name("IT")
                .company(company)
                .build());

        Employee employee = employeeRepository.save(Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .department(department)
                .hireDate(Instant.now())
                .build());

        assertThatThrownBy(() -> companyService.delete(company.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete company with existing employees");

        // Verify nothing was deleted
        assertThat(companyRepository.existsById(company.getId())).isTrue();
        assertThat(departmentRepository.existsById(department.getId())).isTrue();
        assertThat(employeeRepository.existsById(employee.getId())).isTrue();
    }

    @Test
    void delete_WhenNotExists_ThrowsException() {
        CompanyOut created = companyService.create(CompanyIn.builder()
                .name("Temporary")
                .taxId("1234567890")
                .build());
        Long deletedId = created.getId();
        companyRepository.deleteById(deletedId);

        assertThatThrownBy(() -> companyService.delete(deletedId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company(" + deletedId + ") not found");
    }

    @Test
    void exists_WithDeletedCompany_ReturnsFalse() {
        CompanyOut created = companyService.create(CompanyIn.builder()
                .name("Test Company")
                .taxId("1234567890")
                .build());

        assertThat(companyService.exists(created.getId())).isTrue();

        companyService.delete(created.getId());

        assertThat(companyService.exists(created.getId())).isFalse();
    }
}