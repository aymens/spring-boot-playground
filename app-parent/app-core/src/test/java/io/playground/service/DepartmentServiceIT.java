package io.playground.service;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.exception.BusinessException;
import io.playground.exception.NotFoundException;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.test.it.BaseIntegrationTest_Pg16;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class DepartmentServiceIT extends BaseIntegrationTest_Pg16 {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private Faker faker;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        companyRepository.deleteAll();
        
        testCompany = companyRepository.save(Company.builder()
                .name(faker.company().name())
                .taxId(faker.numerify("##########"))
                .build());
    }

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
        Company company = companyRepository.save(Company.builder()
                .name(faker.company().name())
                .taxId(faker.numerify("##########"))
                .build());
        Long deletedId = company.getId();
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
    void getById_WhenExists_ReturnsCorrectDto() {
        Department department = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

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
        Department department = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());
        Long deletedId = department.getId();
        departmentRepository.deleteById(deletedId);

        assertThatThrownBy(() -> departmentService.getById(deletedId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Department(" + deletedId + ") not found");
    }

    @Test
    void getByCompanyId_ReturnsCorrectDtos() {
        List<Department> departments = departmentRepository.saveAll(List.of(
                Department.builder().name(faker.commerce().department()).company(testCompany).build(),
                Department.builder().name(faker.commerce().department()).company(testCompany).build(),
                Department.builder().name(faker.commerce().department()).company(testCompany).build()
        ));

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
        Company company = companyRepository.save(Company.builder()
                .name(faker.company().name())
                .taxId(faker.numerify("##########"))
                .build());
        Long deletedId = company.getId();
        companyRepository.deleteById(deletedId);

        assertThatThrownBy(() -> departmentService.getByCompanyId(deletedId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company(" + deletedId + ") not found");
    }

    @Test
    void delete_WhenExists_DeletesDepartment() {
        Department department = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        departmentService.delete(department.getId(), null);

        assertThat(departmentRepository.existsById(department.getId())).isFalse();
    }

    @Test
    void delete_WithEmployees_RequiresTransferDepartment() {
        Department sourceDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        employeeRepository.save(Employee.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(sourceDept)
                .hireDate(Instant.now())
                .build());

        assertThatThrownBy(() -> departmentService.delete(sourceDept.getId(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Must specify a transfer department ID");
    }

    @Test
    void delete_WithEmployeesAndTransfer_MovesEmployeesAndDeletes() {
        Department sourceDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        Department targetDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        Employee employee = employeeRepository.save(Employee.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(sourceDept)
                .hireDate(Instant.now())
                .build());

        departmentService.delete(sourceDept.getId(), targetDept.getId());

        assertThat(departmentRepository.existsById(sourceDept.getId())).isFalse();
        assertThat(employeeRepository.findById(employee.getId()))
                .isPresent()
                .get()
                .satisfies(emp -> assertThat(emp.getDepartment().getId()).isEqualTo(targetDept.getId()));
    }

    @Test
    void delete_WithEmployeesAndInvalidTransfer_ThrowsNotFoundException() {
        Department sourceDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        Department targetDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());
        Long deletedId = targetDept.getId();
        departmentRepository.deleteById(deletedId);

        employeeRepository.save(Employee.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(sourceDept)
                .hireDate(Instant.now())
                .build());

        assertThatThrownBy(() -> departmentService.delete(sourceDept.getId(), deletedId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Department(" + deletedId + ") not found");
    }

    @Test
    void delete_ToSameDepartment_ThrowsBusinessException() {
        Department department = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        employeeRepository.save(Employee.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(department)
                .hireDate(Instant.now())
                .build());

        assertThatThrownBy(() -> departmentService.delete(department.getId(), department.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot transfer employees to the same department");
    }

    @Test
    void delete_ToDifferentCompany_ThrowsBusinessException() {
        Department sourceDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        Company otherCompany = companyRepository.save(Company.builder()
                .name(faker.company().name())
                .taxId(faker.numerify("##########"))
                .build());

        Department targetDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(otherCompany)
                .build());

        employeeRepository.save(Employee.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(sourceDept)
                .hireDate(Instant.now())
                .build());

        assertThatThrownBy(() -> departmentService.delete(sourceDept.getId(), targetDept.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Target department must be in the same company");
    }

    @Test
    void exists_WithDeletedDepartment_ReturnsFalse() {
        Department department = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());
            
        assertThat(departmentService.exists(department.getId())).isTrue();
        
        departmentService.delete(department.getId(), null);
        
        assertThat(departmentService.exists(department.getId())).isFalse();
    }

    @Test
    void getByCompanyId_WithoutDepartments_ReturnsEmptyList() {
        List<DepartmentOut> results = departmentService.getByCompanyId(testCompany.getId());
        assertThat(results).isEmpty();
    }

    @Test
    void delete_WhenNotExists_ThrowsNotFoundException() {
        Department department = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());
        Long deletedId = department.getId();
        departmentRepository.deleteById(deletedId);

        assertThatThrownBy(() -> departmentService.delete(deletedId, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Department(" + deletedId + ") not found");
    }

    @Test
    void delete_WithEmployeesAndTransfer_MovesAllEmployees() {
        Department sourceDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        Department targetDept = departmentRepository.save(Department.builder()
                .name(faker.commerce().department())
                .company(testCompany)
                .build());

        List<Employee> employees = employeeRepository.saveAll(List.of(
            Employee.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(sourceDept)
                .hireDate(Instant.now())
                .build(),
            Employee.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .department(sourceDept)
                .hireDate(Instant.now())
                .build()
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

    @Test
    void create_WithSameNameDifferentCompanies_Succeeds() {
        Company otherCompany = companyRepository.save(Company.builder()
                .name(faker.company().name())
                .taxId(faker.numerify("##########"))
                .build());

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
}