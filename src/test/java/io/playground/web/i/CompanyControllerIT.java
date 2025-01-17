package io.playground.web.i;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.common.BaseIntegrationTest;
import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
class CompanyControllerIT extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/companies";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();
    }

    @Test
    void createCompany_WithValidInput_ReturnsCreated() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("Acme Corp")
                .taxId("1234567890")
                .build();

        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(allOf(notNullValue(), greaterThan(0))))
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andExpect(jsonPath("$.taxId").value("1234567890"))
                .andReturn();

        CompanyOut created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CompanyOut.class);

        // Verify in DB
        Company savedCompany = companyRepository.findById(created.getId()).orElseThrow();
        assertThat(savedCompany)
                .satisfies(company -> {
                    assertThat(company.getName()).isEqualTo("Acme Corp");
                    assertThat(company.getTaxId()).isEqualTo("1234567890");
                });
    }

    @Test
    void getCompanies_WhenEmpty_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getCompanies_WithExistingData_ReturnsList() throws Exception {
        companyRepository.saveAll(List.of(
                Company.builder().name("First Corp").taxId("1111111111").build(),
                Company.builder().name("Second Corp").taxId("2222222222").build()
        ));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("First Corp"))
                .andExpect(jsonPath("$[1].name").value("Second Corp"));
    }

    @Test
    void createCompany_WithDuplicateTaxId_ReturnsBadRequest() throws Exception {
        String duplicateTaxId = "1234567890";
        companyRepository.save(Company.builder()
                .name("First Corp")
                .taxId(duplicateTaxId)
                .build());

        CompanyIn input = CompanyIn.builder()
                .name("Second Corp")
                .taxId(duplicateTaxId)
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company with tax ID already exists: " + duplicateTaxId));
    }

    @Test
    void deleteCompany_WhenExists_ReturnsNoContent() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name("To Delete")
                .taxId("1234567890")
                .build());

        mockMvc.perform(delete(BASE_URL + "/{id}", company.getId()))
                .andExpect(status().isNoContent());

        assertThat(companyRepository.existsById(company.getId())).isFalse();
    }

    @Test
    void getCompany_WithNonexistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        "io.playground.domain.Company(999) not found"));
    }

    @Test
    void createCompany_WithInvalidInput_ReturnsBadRequest() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("")
                .taxId("123")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Company name is required"))
                .andExpect(jsonPath("$.taxId").value("Tax ID must be exactly 10 digits"));
    }

    @Test
    void createCompany_WithInvalidContentType_ReturnsUnsupportedMediaType() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("Acme")
                .taxId("1234567890")
                .build();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void deleteCompany_WithExistingEmployees_ReturnsBadRequest() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name("Company with Employees")
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

        mockMvc.perform(delete(BASE_URL + "/{id}", company.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(
                        "Cannot delete company with existing employees")));

        assertThat(companyRepository.existsById(company.getId())).isTrue();
        assertThat(departmentRepository.existsById(department.getId())).isTrue();
        assertThat(employeeRepository.existsById(employee.getId())).isTrue();
    }

    @Test
    void deleteCompany_WithEmptyDepartments_Succeeds() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name("Company with Empty Dept")
                .taxId("1234567890")
                .build());

        Department department = departmentRepository.save(Department.builder()
                .name("IT")
                .company(company)
                .build());

        mockMvc.perform(delete(BASE_URL + "/{id}", company.getId()))
                .andExpect(status().isNoContent());

        assertThat(companyRepository.existsById(company.getId())).isFalse();
        assertThat(departmentRepository.existsById(department.getId())).isFalse();
    }
}
