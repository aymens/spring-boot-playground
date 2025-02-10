package io.playground.service.it;

import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import io.playground.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class EmployeeServiceIT extends BaseServiceIntegrationTest_Pg16 {
    @Autowired
    private EmployeeService employeeService;

    private Department testDepartment;

    @BeforeEach
    void init() {
        super.init();
        testDepartment = createTestDepartment(testCompany);
    }

    private EmployeeIn createValidEmployeeInput() {
        return EmployeeIn.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .departmentId(testDepartment.getId())
                .hireDate(Instant.now())
                .build();
    }

    @Nested
    class Create {
        @Test
        void create_WithValidInput_ReturnsCorrectDto() {
            EmployeeIn input = createValidEmployeeInput();

            EmployeeOut result = employeeService.create(input);

            assertThat(result)
                    .satisfies(employee -> {
                        assertThat(employee.getId()).isNotNull().isPositive();
                        assertThat(employee.getFirstName()).isEqualTo(input.getFirstName());
                        assertThat(employee.getLastName()).isEqualTo(input.getLastName());
                        assertThat(employee.getEmail()).isEqualTo(input.getEmail());
                        assertThat(employee.getDepartmentId()).isEqualTo(input.getDepartmentId());
                        assertThat(employee.getHireDate()).isEqualTo(input.getHireDate());
                    });

            Employee saved = employeeRepository.findById(result.getId()).orElseThrow();
            assertThat(saved.getEmail()).isEqualTo(input.getEmail());
        }

        @Test
        void create_WithDuplicateEmail_ThrowsBusinessException() {
            EmployeeIn first = createValidEmployeeInput();
            employeeService.create(first);

            EmployeeIn second = createValidEmployeeInput().toBuilder()
                    .email(first.getEmail())
                    .build();

            assertThatThrownBy(() -> employeeService.create(second))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Employee with email already exists");
        }

        @Test
        void create_WithInvalidDepartment_ThrowsNotFoundException() {
            Long invalidDepartmentId = testDepartment.getId();
            departmentRepository.deleteById(invalidDepartmentId);

            EmployeeIn input = createValidEmployeeInput();

            assertThatThrownBy(() -> employeeService.create(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Department(" + invalidDepartmentId + ") not found");
        }
    }

    @Nested
    class Read {
        @Test
        void getById_WhenExists_ReturnsCorrectDto() {
            EmployeeIn input = createValidEmployeeInput();
            EmployeeOut created = employeeService.create(input);

            EmployeeOut result = employeeService.getById(created.getId());

            assertThat(result)
                    .satisfies(employee -> {
                        assertThat(employee.getId()).isEqualTo(created.getId());
                        assertThat(employee.getFirstName()).isEqualTo(created.getFirstName());
                        assertThat(employee.getLastName()).isEqualTo(created.getLastName());
                        assertThat(employee.getEmail()).isEqualTo(created.getEmail());
                        assertThat(employee.getDepartmentId()).isEqualTo(created.getDepartmentId());
                    });
        }

        @Test
        void getById_WhenNotExists_ThrowsNotFoundException() {
            Employee employee = createTestEmployee(testDepartment);
            Long deletedId = employee.getId();
            employeeRepository.deleteById(deletedId);

            assertThatThrownBy(() -> employeeService.getById(deletedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Employee(" + deletedId + ") not found");
        }

        @Test
        void getByDepartmentId_ReturnsAllEmployees() {
            Employee employee1 = createTestEmployee(testDepartment);
            Employee employee2 = createTestEmployee(testDepartment);

            List<EmployeeOut> results = employeeService.getByDepartmentId(testDepartment.getId());

            assertThat(results)
                    .hasSize(2)
                    .extracting(EmployeeOut::getId)
                    .containsExactlyInAnyOrder(employee1.getId(), employee2.getId());
        }

        @Test
        void getByDepartmentId_WhenDepartmentNotExists_ThrowsBusinessException() {
            Long invalidDepartmentId = testDepartment.getId();
            departmentRepository.deleteById(invalidDepartmentId);

            assertThatThrownBy(() -> employeeService.getByDepartmentId(invalidDepartmentId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Department not found: " + invalidDepartmentId);
        }

        @Test
        void getByDepartmentId_WithNoEmployees_ReturnsEmptyList() {
            List<EmployeeOut> results = employeeService.getByDepartmentId(testDepartment.getId());
            assertThat(results).isEmpty();
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenExists_DeletesEmployee() {
            Employee employee = createTestEmployee(testDepartment);

            employeeService.delete(employee.getId());

            assertThat(employeeRepository.existsById(employee.getId())).isFalse();
        }

        @Test
        void delete_WhenNotExists_ThrowsNotFoundException() {
            Employee employee = createTestEmployee(testDepartment);
            Long deletedId = employee.getId();
            employeeRepository.deleteById(deletedId);

            assertThatThrownBy(() -> employeeService.delete(deletedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Employee(" + deletedId + ") not found");
        }
    }

    @Nested
    class Exists {
        @Test
        void exists_WithDeletedEmployee_ReturnsFalse() {
            Employee employee = createTestEmployee(testDepartment);
            assertThat(employeeService.exists(employee.getId())).isTrue();

            employeeService.delete(employee.getId());

            assertThat(employeeService.exists(employee.getId())).isFalse();
        }
    }
}