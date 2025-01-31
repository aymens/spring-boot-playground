package io.playground.web;

import io.playground.conf.ConditionalOnDataGeneratorEnabled;
import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.exception.InvalidIdRangeException;
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
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.datafaker.Faker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/data-gen")
@ConditionalOnDataGeneratorEnabled
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DataGeneratorController {

    private static final Pattern ID_PATTERN = Pattern.compile("(\\d+)(?:-(\\d+))?");

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
            @Parameter(description = "One or more ID ranges in the format 'number(-number)?'. For example, '1', '2-4', '5'.")
            @RequestParam(defaultValue = "5") int numEmployees) {
        var department = generateDepartment(companyId);
        generateEmployees(department.getId(), numEmployees);
        return ResponseEntity.ok(getCompanyEagerly(companyId));
    }

    @PostMapping("/departments/{departmentId}/employees")
    public ResponseEntity<Department> addEmployeesToDepartment(
            @PathVariable Long departmentId,
            @Parameter(
                    description = "One or more ID ranges in the format 'number(-number)?'. For example, '1', '2-4', '5'."
            )
            @RequestParam(defaultValue = "5") int numEmployees) {
        return ResponseEntity.ok(
                generateEmployees(departmentId, numEmployees));
    }

    @DeleteMapping("/companies")
    public ResponseEntity<Void> deleteCompanies(
            @Parameter(description = "One or more ID ranges in the format 'number(-number)?'. For example, '1', '2-4', '5'.")
            @RequestParam(name = "ids", defaultValue = "123") String... idRanges) {
        deleteEntities(parseIdRanges(idRanges), companyRepository);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/departments")
    public ResponseEntity<Void> deleteDepartments(
            @Parameter(description = "One or more ID ranges in the format 'number(-number)?'. For example, '1', '2-4', '5'.")
            @RequestParam(name = "ids", defaultValue = "123") String... idRanges) {
        deleteEntities(parseIdRanges(idRanges), departmentRepository);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/employees")
    public ResponseEntity<Void> deleteEmployees(
            @Parameter(description = "One or more ID ranges in the format 'number(-number)?'. For example, '1', '2-4', '5'.")
            @RequestParam(name = "ids", defaultValue = "123") String... idRanges) {
        deleteEntities(parseIdRanges(idRanges), employeeRepository);
        return ResponseEntity.noContent().build();
    }

    private <T> void deleteEntities(Stream<Long> ids, JpaRepository<T, Long> repository) {
        val toDelete = ids.filter(repository::existsById).toList();
        if (!toDelete.isEmpty()) {
            repository.deleteAllById(toDelete);
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
        } while (departmentRepository.existsByNameAndCompany_Id(departmentName, companyId));

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
                    EmployeeIn employee = EmployeeIn.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(generateEmail(firstName, lastName))
                            .departmentId(departmentId)
                            .hireDate(generateRandomPastDate())
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

    private String generateEmail(String firstName, String lastName) {
        val localPart = String
                        .join(".", firstName.toLowerCase(), lastName.toLowerCase())
                        .replaceAll("[^a-z0-9.]", "");
        var email = faker.internet().emailAddress(localPart);
        while (employeeRepository.existsByEmail(email)) {
            log.warn("Email already exists: {}", email);
            email = faker.internet().emailAddress(localPart + "." + faker.number().digits(3));
        }
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

    private Stream<Long> parseIdRanges(String... idRanges) {
        if (idRanges == null || idRanges.length == 0) {
            throw new InvalidIdRangeException("No IDs provided");
        }

        return Arrays.stream(idRanges)
                .flatMap(range -> {
                    var matcher = ID_PATTERN.matcher(range.trim());
                    if (!matcher.matches()) {
                        throw new InvalidIdRangeException("Invalid format: " + range);
                    }

                    long start = Long.parseLong(matcher.group(1));
                    var end = Long.parseLong(Objects.requireNonNullElse(matcher.group(2), start) + "");

                    if (start <= 0 || end < start) {
                        throw new InvalidIdRangeException("Invalid range: " + range);
                    }

                    return LongStream.rangeClosed(start, end).boxed();
                })
                .distinct();
    }
}