package io.playground.service.it;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class CompanyServiceIT extends BaseServiceIntegrationTest_Pg16 {
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
            assertThat(savedCompany.getName()).isEqualTo(input.getName());
            assertThat(savedCompany.getTaxId()).isEqualTo(input.getTaxId());
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