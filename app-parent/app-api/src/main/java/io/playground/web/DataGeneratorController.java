package io.playground.web;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.model.CompanyIn;
import io.playground.model.DepartmentIn;
import io.playground.model.EmployeeIn;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.service.CompanyService;
import io.playground.service.DepartmentService;
import io.playground.service.EmployeeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/data-gen")
@ConditionalOnProperty(
        prefix = "playground.api.rest",
        name = "data-generator.enabled",
        havingValue = "true"
)
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DataGeneratorController {

    private final CompanyService companyService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final Faker faker;

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/company")
    public ResponseEntity<Company> createCompany(
            @RequestParam(defaultValue = "3") int numDepartments,
            @RequestParam(defaultValue = "5") int employeesPerDepartment) {
        return ResponseEntity.ok(generateCompany(numDepartments, employeesPerDepartment));
    }

    @PostMapping("/companies")
    public ResponseEntity<List<Company>> createCompanies(
            @RequestParam(defaultValue = "3") int numCompanies,
            @RequestParam(defaultValue = "3") int numDepartments,
            @RequestParam(defaultValue = "5") int employeesPerDepartment) {
        return ResponseEntity.ok(
                IntStream.range(0, numCompanies)
                        .mapToObj(_ -> generateCompany(numDepartments, employeesPerDepartment))
                        .toList());
    }

    @PostMapping("/companies/{companyId}/departments")
    public ResponseEntity<Company> addDepartmentToCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "5") int numEmployees) {
        var department = departmentService.create(
                DepartmentIn.builder()
                        .name(faker.commerce().department())
                        .companyId(companyId)
                        .build());

        generateEmployees(department.getId(), numEmployees);
        return ResponseEntity.ok(getCompanyEagerly(companyId));
    }

    @PostMapping("/departments/{departmentId}/employees")
    public ResponseEntity<Department> addEmployeesToDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "5") int numEmployees) {
        return ResponseEntity.ok(
                generateEmployees(departmentId, numEmployees));
    }

    private Company generateCompany(int numDepartments, int employeesPerDepartment) {
        var company = companyService.create(CompanyIn.builder()
                .name(faker.company().name())
                .taxId(generateRandomTaxId())
                .build());

        IntStream.range(0, numDepartments)
                .forEach(_ -> {
                    var department = departmentService.create(DepartmentIn.builder()
                            .name(faker.commerce().department())
                            .companyId(company.getId())
                            .build());

                    generateEmployees(department.getId(), employeesPerDepartment);
                });

        return getCompanyEagerly(company.getId());
    }

    private Department generateEmployees(Long departmentId, int count) {
        IntStream.range(0, count)
                .forEach(_ -> employeeService.create(EmployeeIn.builder()
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .email(generateEmail())
                        .departmentId(departmentId)
                        .hireDate(generateRandomPastDate())
                        .build()));
        return getDepartmentEagerly(departmentId);
    }

    private String generateRandomTaxId() {
        String taxId;
        do {
            taxId = faker.numerify("##########");
        } while (companyRepository.existsByTaxId(taxId));
        return taxId;
    }

    private String generateEmail() {
        String email;
        do {
            email = faker.internet().emailAddress();
        } while (employeeRepository.existsByEmail(email));
        return email;
    }

    private Instant generateRandomPastDate() {
        return Instant.now().minus(
                faker.number().numberBetween(1, 3650),
                ChronoUnit.DAYS);
    }

    private Company getCompanyEagerly(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow();
        entityManager.flush();
        return company;
    }

    private Department getDepartmentEagerly(Long departmentId) {
        Department department = departmentRepository.findById(departmentId).orElseThrow();
        entityManager.flush();
        return department;
    }
}