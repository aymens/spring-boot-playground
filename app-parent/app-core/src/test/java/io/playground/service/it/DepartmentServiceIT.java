package io.playground.service.it;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class DepartmentServiceIT extends BaseServiceIntegrationTest {
    @Autowired
    private DepartmentService departmentService;

    @Nested
    class Create {
        @Test
        void create_WithValidInput_ReturnsCorrectDto() {
            String departmentName = faker.commerce().department();
            DepartmentIn input = DepartmentIn.builder()
                    .name(departmentName)
                    .companyId(testCompany.getId())
                    .build();

            DepartmentOut result = departmentService.create(input);

            assertThat(result)
                    .isNotNull()
                    .satisfies(dept -> {
                        assertThat(dept.getId()).isNotNull();
                        assertThat(dept.getName()).isEqualTo(departmentName);
                        assertThat(dept.getCompanyId()).isEqualTo(testCompany.getId());
                    });

            Department saved = departmentRepository.findById(result.getId()).orElseThrow();
            assertThat(saved.getName()).isEqualTo(departmentName);
            assertThat(saved.getCompany().getId()).isEqualTo(testCompany.getId());
        }

        @Test
        void create_WithInvalidCompanyId_ThrowsNotFoundException() {
            Long deletedId = testCompany.getId();
            companyRepository.deleteById(deletedId);

            DepartmentIn input = DepartmentIn.builder()
                    .name(faker.commerce().department())
                    .companyId(deletedId)
                    .build();

            assertThatThrownBy(() -> departmentService.create(input))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Company(" + deletedId + ") not found");
        }

        @Test
        void create_WithDuplicateName_ThrowsBusinessException() {
            String departmentName = faker.commerce().department();
            DepartmentIn first = DepartmentIn.builder()
                    .name(departmentName)
                    .companyId(testCompany.getId())
                    .build();

            departmentService.create(first);

            DepartmentIn second = DepartmentIn.builder()
                    .name(departmentName)
                    .companyId(testCompany.getId())
                    .build();

            assertThatThrownBy(() -> departmentService.create(second))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Department already exists in company");
        }

        @Test
        void create_WithSameNameDifferentCase_ThrowsBusinessException() {
            String departmentName = faker.commerce().department();
            departmentService.create(DepartmentIn.builder()
                    .name(departmentName)
                    .companyId(testCompany.getId())
                    .build());

            DepartmentIn input = DepartmentIn.builder()
                    .name(departmentName.toUpperCase())
                    .companyId(testCompany.getId())
                    .build();

            assertThatThrownBy(() -> departmentService.create(input))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Department already exists in company");
        }

        @Test
        void create_WithSameNameDifferentCompanies_Succeeds() {
            Company otherCompany = createTestCompany();

            String departmentName = faker.commerce().department();
            DepartmentIn first = DepartmentIn.builder()
                    .name(departmentName)
                    .companyId(testCompany.getId())
                    .build();

            DepartmentIn second = DepartmentIn.builder()
                    .name(departmentName)
                    .companyId(otherCompany.getId())
                    .build();

            departmentService.create(first);
            DepartmentOut result = departmentService.create(second);

            assertThat(result.getName()).isEqualTo(departmentName);
            assertThat(result.getCompanyId()).isEqualTo(otherCompany.getId());
        }
    }

    @Nested
    class Read {
        @Test
        void getById_WhenExists_ReturnsCorrectDto() {
            Department department = createTestDepartment(testCompany);

            DepartmentOut result = departmentService.getById(department.getId());

            assertThat(result)
                    .isNotNull()
                    .satisfies(dept -> {
                        assertThat(dept.getId()).isEqualTo(department.getId());
                        assertThat(dept.getName()).isEqualTo(department.getName());
                        assertThat(dept.getCompanyId()).isEqualTo(testCompany.getId());
                    });
        }

        @Test
        void getById_WhenNotExists_ThrowsNotFoundException() {
            Department department = createTestDepartment(testCompany);
            Long deletedId = department.getId();
            departmentRepository.deleteById(deletedId);

            assertThatThrownBy(() -> departmentService.getById(deletedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Department(" + deletedId + ") not found");
        }

        @Test
        void getByCompanyId_ReturnsCorrectDtos() {
            List<Department> departments = createTestDepartments(testCompany, 3);

            List<DepartmentOut> results = departmentService.getByCompanyId(testCompany.getId());

            assertThat(results)
                    .hasSize(3)
                    .extracting(DepartmentOut::getId)
                    .containsExactlyInAnyOrderElementsOf(
                            departments.stream().map(Department::getId).toList()
                    );

            assertThat(results)
                    .extracting(DepartmentOut::getName)
                    .containsExactlyInAnyOrderElementsOf(
                            departments.stream().map(Department::getName).toList()
                    );

            assertThat(results)
                    .extracting(DepartmentOut::getCompanyId)
                    .containsOnly(testCompany.getId());
        }

        @Test
        void getByCompanyId_WithInvalidId_ThrowsNotFoundException() {
            Long deletedId = testCompany.getId();
            companyRepository.deleteById(deletedId);

            assertThatThrownBy(() -> departmentService.getByCompanyId(deletedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Company(" + deletedId + ") not found");
        }

        @Test
        void getByCompanyId_WithoutDepartments_ReturnsEmptyList() {
            List<DepartmentOut> results = departmentService.getByCompanyId(testCompany.getId());
            assertThat(results).isEmpty();
        }
    }

    @Nested
    class Delete {
        @Test
        void delete_WhenExists_DeletesDepartment() {
            Department department = createTestDepartment(testCompany);

            departmentService.delete(department.getId(), null);

            assertThat(departmentRepository.existsById(department.getId())).isFalse();
        }

        @Test
        void delete_WhenNotExists_ThrowsNotFoundException() {
            Department department = createTestDepartment(testCompany);
            Long deletedId = department.getId();
            departmentRepository.deleteById(deletedId);

            assertThatThrownBy(() -> departmentService.delete(deletedId, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Department(" + deletedId + ") not found");
        }

        @Test
        void delete_WithEmployees_RequiresTransferDepartment() {
            Department sourceDept = createTestDepartment(testCompany);
            employeeRepository.save(createTestEmployee(sourceDept));

            assertThatThrownBy(() -> departmentService.delete(sourceDept.getId(), null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Must specify a transfer department ID");
        }

        @Test
        void delete_WithEmployeesAndTransfer_MovesEmployeesAndDeletes() {
            Department sourceDept = createTestDepartment(testCompany);
            Department targetDept = createTestDepartment(testCompany);
            Employee employee = createTestEmployee(sourceDept);

            departmentService.delete(sourceDept.getId(), targetDept.getId());

            assertThat(departmentRepository.existsById(sourceDept.getId())).isFalse();
            assertThat(employeeRepository.findById(employee.getId()))
                    .isPresent()
                    .get()
                    .satisfies(emp -> assertThat(emp.getDepartment().getId()).isEqualTo(targetDept.getId()));
        }

        @Test
        void delete_WithEmployeesAndInvalidTransfer_ThrowsNotFoundException() {
            Department sourceDept = createTestDepartment(testCompany);
            Department targetDept = createTestDepartment(testCompany);
            Long deletedId = targetDept.getId();
            departmentRepository.deleteById(deletedId);

            createTestEmployee(sourceDept);

            assertThatThrownBy(() -> departmentService.delete(sourceDept.getId(), deletedId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Department(" + deletedId + ") not found");
        }

        @Test
        void delete_ToSameDepartment_ThrowsBusinessException() {
            Department department = createTestDepartment(testCompany);
            createTestEmployee(department);

            assertThatThrownBy(() -> departmentService.delete(department.getId(), department.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot transfer employees to the same department");
        }

        @Test
        void delete_ToDifferentCompany_ThrowsBusinessException() {
            Department sourceDept = createTestDepartment(testCompany);
            Company otherCompany = createTestCompany();
            Department targetDept = createTestDepartment(otherCompany);
            createTestEmployee(sourceDept);

            assertThatThrownBy(() -> departmentService.delete(sourceDept.getId(), targetDept.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Target department must be in the same company");
        }

        @Test
        void delete_WithEmployeesAndTransfer_MovesAllEmployees() {
            Department sourceDept = createTestDepartment(testCompany);
            Department targetDept = createTestDepartment(testCompany);

            List<Employee> employees = employeeRepository.saveAll(List.of(
                    createTestEmployee(sourceDept),
                    createTestEmployee(sourceDept)
            ));

            departmentService.delete(sourceDept.getId(), targetDept.getId());

            assertThat(departmentRepository.existsById(sourceDept.getId())).isFalse();

            List<Employee> transferredEmployees = employeeRepository.findByDepartmentId(targetDept.getId());
            assertThat(transferredEmployees)
                    .hasSize(2)
                    .extracting(Employee::getId)
                    .containsExactlyInAnyOrderElementsOf(
                            employees.stream().map(Employee::getId).toList()
                    );
        }
    }

    @Nested
    class Exists {
        @Test
        void exists_WithDeletedDepartment_ReturnsFalse() {
            Department department = createTestDepartment(testCompany);
            assertThat(departmentService.exists(department.getId())).isTrue();

            departmentService.delete(department.getId(), null);

            assertThat(departmentService.exists(department.getId())).isFalse();
        }
    }
}