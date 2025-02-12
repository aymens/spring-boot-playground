package io.playground.it.service;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.it.BaseCoreIntegrationTest;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.IntStream;

import static io.playground.test.data.PageAssert.assertThatPage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class CompanyServiceIT extends BaseCoreIntegrationTest {
    @Autowired
    private CompanyService companyService;

    private CompanyIn createValidCompanyInput() {
        return CompanyIn.builder()
                .name(faker.company().name())
                .taxId(faker.numerify("##########"))
                .build();
    }

    @Nested
    class Create {
        @Test
        void create_WithValidInput_ReturnsCorrectDto() {
            CompanyIn input = createValidCompanyInput();

            CompanyOut result = companyService.create(input);

            assertThat(result)
                    .satisfies(company -> {
                        assertThat(company.getId()).isNotNull().isPositive();
                        assertThat(company.getName()).isEqualTo(input.getName());
                        assertThat(company.getTaxId()).isEqualTo(input.getTaxId());
                    });

            Company savedCompany = companyRepository.findById(result.getId()).orElseThrow();
            assertThat(savedCompany)
                    .extracting(Company::getName, Company::getTaxId)
                    .containsExactly(input.getName(), input.getTaxId());
        }

        @Test
        void create_WithDuplicateTaxId_ThrowsBusinessException() {
            CompanyIn first = createValidCompanyInput();
            companyService.create(first);

            CompanyIn second = createValidCompanyInput().toBuilder()
                    .taxId(first.getTaxId())
                    .build();

            assertThatThrownBy(() -> companyService.create(second))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Company with tax ID already exists");
        }
    }

    @Nested
    class Read {
        @Test
        void getById_WhenExists_ReturnsCorrectDto() {
            CompanyIn input = createValidCompanyInput();
            CompanyOut created = companyService.create(input);

            CompanyOut result = companyService.getById(created.getId());

            assertThat(result)
                    .satisfies(company -> {
                        assertThat(company.getId()).isEqualTo(created.getId());
                        assertThat(company.getName()).isEqualTo(created.getName());
                        assertThat(company.getTaxId()).isEqualTo(created.getTaxId());
                    });
        }

        @Test
        void getById_WhenNotExists_ThrowsNotFoundException() {
            Long deletedId = testCompany.getId();
            companyRepository.deleteById(deletedId);

            assertThatThrownBy(() -> companyService.getById(deletedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Company(" + deletedId + ") not found");
        }

        @Test
        void getAll_ReturnsCorrectDtos() {
            companyRepository.deleteAll();
            CompanyOut created1 = companyService.create(createValidCompanyInput());
            CompanyOut created2 = companyService.create(createValidCompanyInput());

            List<CompanyOut> results = companyService.getAll();

            assertThat(results)
                    .hasSize(2)
                    .extracting(CompanyOut::getId)
                    .containsExactlyInAnyOrder(created1.getId(), created2.getId());
        }

        @Test
        void findCompaniesWithMinDepartments_ReturnsExpectedCompanies() {
            // Create test companies with varying department counts
            Company company1 = createTestCompany();
            createTestDepartments(company1, 5);

            Company company2 = createTestCompany();
            createTestDepartments(company2, 2);

            Company company3 = createTestCompany();
            createTestDepartments(company3, 4);

            // Test with minDepartments = 4
            Page<CompanyOut> result = companyService.findCompaniesWithMinDepartments(4, Pageable.unpaged());

            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .extracting(CompanyOut::getId)
                    .containsExactlyInAnyOrder(company1.getId(), company3.getId());
            ;
        }

        @Test
        void findCompaniesWithMinEmployees_ReturnsExpectedCompanies() {
            // Create companies with varying employee counts
            Company company1 = createTestCompany();
            Department dept1 = createTestDepartment(company1);
            IntStream.range(0, 5).forEach(_ -> createTestEmployee(dept1));

            Company company2 = createTestCompany();
            Department dept2 = createTestDepartment(company2);
            IntStream.range(0, 2).forEach(_ -> createTestEmployee(dept2));

            // Test with minEmployees = 4
            Page<CompanyOut> result = companyService.findCompaniesWithMinEmployees(4, Pageable.unpaged());

            assertThat(result)
                    .isNotNull()
                    .hasSize(1)
                    .extracting(CompanyOut::getId)
                    .containsExactly(company1.getId());
        }

        @Test
        void findCompaniesWithMinDepartmentsAndEmployees_ReturnsExpectedCompanies() {
            // Create companies with varying department and employee counts
            Company company1 = createTestCompany();
            List<Department> company1Depts = createTestDepartments(company1, 5);
            company1Depts.forEach(dept ->
                    IntStream.range(0, 3).forEach(_ -> createTestEmployee(dept)));//15

            Company company2 = createTestCompany();
            List<Department> company2Depts = createTestDepartments(company2, 2);
            company2Depts.forEach(dept ->
                    IntStream.range(0, 4).forEach(_ -> createTestEmployee(dept)));//8

            Company company3 = createTestCompany();
            List<Department> company3Depts = createTestDepartments(company3, 4);
            company3Depts.forEach(dept ->
                    IntStream.range(0, 4).forEach(_ -> createTestEmployee(dept)));//16

            // Test with minDepartments = 4 and minEmployees = 12
            Page<CompanyOut> result = companyService.findCompaniesWithMinDepartmentsAndEmployees(
                    4,
                    12,
                    // Pageable.unpaged(Sort.sort(... doesn't work
                    PageRequest.of(0,
                            Integer.MAX_VALUE,
                            Sort.sort(Company.class)
                                    .by(Company::getId)
                                    .descending())

            );

            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .extracting(CompanyOut::getId)
                    .containsExactly(company3.getId(), company1.getId());
        }

        @Test
        void findCompaniesWithMinDepartmentsAndEmployees_WithPagination_ReturnsCorrectPage() {
            // Create several companies that meet the criteria
            List<Company> companies = IntStream.range(0, 5)
                    .mapToObj(_ -> {
                        Company company = createTestCompany();
                        List<Department> depts = createTestDepartments(company, 4);
                        depts.forEach(dept ->
                                IntStream.range(0, 4).forEach(_ -> createTestEmployee(dept)));
                        return company;
                    })
                    .toList();

            // Test with pagination
//            Pageable pageable = PageRequest.of(1, 2, Sort.by("id").ascending());

            Pageable pageable =
                    PageRequest.of(
                            1,
                            2,
                            Sort.sort(Company.class)
                                    .by(Company::getId)
                                    .ascending()
                    );

            Page<CompanyOut> result = companyService.findCompaniesWithMinDepartmentsAndEmployees(
                    4, 12, pageable);

            assertThatPage(result)
                    .isNotNull()
                    .hasPageSize(2)
                    .hasTotalElements(5)
                    .hasTotalPages(3)
                    .hasPageNumber(1);
        }

        @Test
        void findCompaniesWithMinDepartmentsAndEmployees_WhenNoneMatch_ReturnsEmptyPage() {
            // Create company that doesn't meet criteria
            Company company = createTestCompany();
            List<Department> departments = createTestDepartments(company, 2); // Less than min departments
            departments.forEach(dept ->
                    IntStream.range(0, 2).forEach(_ -> createTestEmployee(dept))); // Less than min employees

            Page<CompanyOut> result = companyService.findCompaniesWithMinDepartmentsAndEmployees(
                    4, 12, Pageable.unpaged());

            assertThatPage(result)
                    .isNotNull()
                    .hasEmptyContent()
                    .hasTotalElements(0);
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenExistsWithoutEmployees_DeletesCompanyAndDepartments() {
            List<Department> departments = createTestDepartments(testCompany, 5);
            List<Long> departmentIds = departments.stream()
                    .map(Department::getId)
                    .toList();

            val companyId = testCompany.getId();
            companyService.delete(companyId);

            assertThat(companyRepository.existsById(companyId)).isFalse();
            assertThat(departmentRepository.findAllById(departmentIds)).isEmpty();
        }

        @Test
        void delete_WithEmployees_ThrowsBusinessException() {
            Department department = createTestDepartment(testCompany);
            Employee employee = createTestEmployee(department);

            val companyId = testCompany.getId();
            assertThatThrownBy(() -> companyService.delete(companyId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot delete company with existing employees");

            assertThat(companyRepository.existsById(companyId)).isTrue();
            assertThat(departmentRepository.existsById(department.getId())).isTrue();
            assertThat(employeeRepository.existsById(employee.getId())).isTrue();
        }

        @Test
        void delete_WhenNotExists_ThrowsNotFoundException() {
            Long deletedId = testCompany.getId();
            companyRepository.deleteById(deletedId);

            assertThatThrownBy(() -> companyService.delete(deletedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Company(" + deletedId + ") not found");
        }
    }

    @Nested
    class Exists {
        @Test
        void exists_WithDeletedCompany_ReturnsFalse() {
            assertThat(companyService.exists(testCompany.getId())).isTrue();

            companyService.delete(testCompany.getId());

            assertThat(companyService.exists(testCompany.getId())).isFalse();
        }
    }
}