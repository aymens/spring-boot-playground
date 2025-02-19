package io.playground.web;

import io.playground.configuration.annotations.ConditionalOnDataGeneratorEnabled;
import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.exception.InvalidIdRangeException;
import io.playground.helper.FakerHelper;
import io.playground.model.CompanyIn;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.model.EmployeeIn;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import io.playground.service.CompanyService;
import io.playground.service.DepartmentService;
import io.playground.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.datafaker.Faker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static io.playground.helper.NumberUtils.randomBigDecimal;
import static java.lang.Long.parseLong;
import static java.util.Objects.requireNonNullElse;

@RestController
@RequestMapping("api/data-gen")
@ConditionalOnDataGeneratorEnabled
@Slf4j
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasAnyRole('ROLE_app_user', 'ROLE_app_admin')")
public class DataGeneratorController {

    private static final Pattern IDS_PATTERN = Pattern.compile("(\\d+)(?:-(\\d+))?");

    private final CompanyService companyService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final Faker faker;
    private final FakerHelper fakerHelper;

    @PersistenceContext
    private EntityManager entityManager;

    @PostMapping("/company")
    public ResponseEntity<Company> createCompany(
            @RequestParam(defaultValue = "3") int numDepartments,
            @RequestParam(defaultValue = "5") int employeesPerDepartment) {
        return ResponseEntity.ok(generateCompany(numDepartments, employeesPerDepartment));
    }

    @PostMapping("/companies")
    public ResponseEntity<List<Company>> createCompanies(@RequestParam(defaultValue = "3") int numCompanies,
                                                         @RequestParam(defaultValue = "3") int numDepartments,
                                                         @RequestParam(defaultValue = "5") int employeesPerDepartment) {
        return ResponseEntity.ok(
                IntStream.range(0, numCompanies)
                        .mapToObj(_ -> generateCompany(numDepartments, employeesPerDepartment))
                        .toList());
    }

    @PostMapping("/companies/{companyId}/departments")
    @Operation(summary = "Add a department to a company",
            description = "Adds a new department to the specified company and generates employees for it.")
    public ResponseEntity<Company> addDepartmentToCompany(
            @Parameter(description = "The ID of the company to add the department to", required = true)
            @PathVariable Long companyId,
            @Parameter(description = "The number of employees to generate for the new department", example = "5")
            @RequestParam(defaultValue = "5") int numEmployees) {
        var department = generateDepartment(companyId);
        generateEmployees(department.getId(), numEmployees);
        return ResponseEntity.ok(getCompanyEagerly(companyId));
    }

    @PostMapping("/departments/{departmentId}/employees")
    public ResponseEntity<Department> addEmployeesToDepartment(@PathVariable Long departmentId,
                                                               @RequestParam(defaultValue = "5") int numEmployees) {
        return ResponseEntity.ok(
                generateEmployees(departmentId, numEmployees));
    }

    @DeleteMapping("/companies")
    public ResponseEntity<Void> deleteCompanies(
            @Parameter(description = "One or more ID ranges in the format 'number(-number)?'. For example, '1', '2-4', '5'.")
            @RequestParam(name = "ids")
            String... idRanges) {
        deleteEntities(parseIdRanges(idRanges), companyRepository);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/departments")
    public ResponseEntity<Void> deleteDepartments(
            @Parameter(description = "One or more ID ranges in the format 'number(-number)?'. For example, '1', '2-4', '5'.")
            @RequestParam(name = "ids") String... idRanges) {
        deleteEntities(parseIdRanges(idRanges), departmentRepository);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employees")
    public ResponseEntity<Void> deleteEmployees(
            @Parameter(description = "One or more ID ranges in the format 'number(-number)?'. For example, '1', '2-4', '5'.")
            @RequestParam(name = "ids") String... idRanges) {
        deleteEntities(parseIdRanges(idRanges), employeeRepository);
        return ResponseEntity.noContent().build();
    }

    private <T> void deleteEntities(List<Long> idsToDelete, JpaRepository<T, Long> repository) {
        if (!idsToDelete.isEmpty()) {
            repository.deleteAllByIdInBatch(idsToDelete);
        }
    }

    private Company generateCompany(int numDepartments, int employeesPerDepartment) {
        var company = companyService.create(CompanyIn.builder()
                .name(faker.company().name())
                .taxId(generateRandomTaxId())
                .build());

        IntStream.range(0, numDepartments)
                .forEach(_ -> {
                    val department = generateDepartment(company.getId());
                    generateEmployees(department.getId(), employeesPerDepartment);
                });

        return getCompanyEagerly(company.getId());
    }

    private DepartmentOut generateDepartment(Long companyId) {
        String departmentName;
        do {
            departmentName = faker.commerce().department();
        } while (departmentRepository.existsByNameIgnoreCaseAndCompany_Id(departmentName, companyId));

        return departmentService.create(DepartmentIn.builder()
                .name(departmentName)
                .companyId(companyId)
                .build());
    }

    private Department generateEmployees(Long departmentId, int count) {
        IntStream.range(0, count)
                .forEach(_ -> {
                    val firstName = faker.name().firstName();
                    val lastName = faker.name().lastName();
                    val email = fakerHelper.generateEmail(firstName, lastName, Predicate.not(employeeRepository::existsByEmail));
                    EmployeeIn employee = EmployeeIn.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .departmentId(departmentId)
                            .hireDate(generateRandomPastDate())
                            .salary(randomBigDecimal())
                            .build();
                    employeeService.create(employee);
                });
        return getDepartmentEagerly(departmentId);
    }

    private String generateRandomTaxId() {
        String taxId;
        do {
            taxId = faker.numerify("##########");
        } while (companyRepository.existsByTaxId(taxId));
        return taxId;
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

    private List<Long> parseIdRanges(String... idRanges) {
        if (idRanges == null || idRanges.length == 0) {
            throw new InvalidIdRangeException("No IDs provided");
        }

        return Arrays.stream(idRanges)
                .flatMap(range -> {
                    var matcher = IDS_PATTERN.matcher(range.trim());
                    if (!matcher.matches()) {
                        throw new InvalidIdRangeException("Invalid format: " + range);
                    }

                    long start = parseLong(matcher.group(1));
                    var end = parseLong(requireNonNullElse(matcher.group(2), String.valueOf(start)));

                    if (start <= 0 || end < start) {
                        throw new InvalidIdRangeException("Invalid range: " + range);
                    }

                    return LongStream.rangeClosed(start, end).boxed();
                })
                .distinct()
                .toList();
    }
}