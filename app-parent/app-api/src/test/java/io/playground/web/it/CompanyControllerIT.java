package io.playground.web.it;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.test.security.annotations.WithMockJwtAuth;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static io.playground.helper.NumberUtils.randomBigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockJwtAuth(roles = {"ROLE_app_user"})
@Slf4j
class CompanyControllerIT extends BaseControllerIntegrationTest_Pg16 {

    private static final String BASE_URL = "/api/companies";

    @Nested
    class Create {
        @Test
        void createCompany_WithValidInput_ReturnsCreated() throws Exception {
            var companyIn = CompanyIn.builder()
                    .name(faker.company().name())
                    .taxId(faker.numerify("##########"))
                    .build();

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(companyIn)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(allOf(notNullValue(), greaterThan(0))))
                    .andExpect(jsonPath("$.name").value(companyIn.getName()))
                    .andExpect(jsonPath("$.taxId").value(companyIn.getTaxId()))
                    .andReturn();

            CompanyOut created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    CompanyOut.class);

            // Verify in DB
            Company savedCompany = companyRepository.findById(created.getId()).orElseThrow();
            assertThat(savedCompany)
                    //version 1
                    .satisfies(company -> {
                        assertThat(company.getName()).isEqualTo(companyIn.getName());
                        assertThat(company.getTaxId()).isEqualTo(companyIn.getTaxId());
                    })
                    //version 2
                    .extracting(Company::getName, Company::getTaxId)
                    .containsExactlyInAnyOrder(companyIn.getName(), companyIn.getTaxId());
        }

        @Test
        void createCompany_WithDuplicateTaxId_ReturnsBadRequest() throws Exception {
            String duplicateTaxId = faker.numerify("##########");
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
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Company with tax ID already exists: " + duplicateTaxId));
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
                    .andDo(print())
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
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());
        }


    }

    @Nested
    class Get {
        @Test
        void getCompanies_WhenEmpty_ReturnsEmptyList() throws Exception {
            companyRepository.deleteAll();
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        void getCompanies_WithExistingData_ReturnsList() throws Exception {
            companyRepository.deleteAll();
            companyRepository.saveAll(List.of(
                    Company.builder().name("First Corp").taxId("1111111111").build(),
                    Company.builder().name("Second Corp").taxId("2222222222").build()
            ));

            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].name").value("First Corp"))
                    .andExpect(jsonPath("$.content[1].name").value("Second Corp"));
        }

        @Test
        void getCompany_WithNonexistentId_ReturnsNotFound() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(
                            containsString("Company(999) not found")));
        }

        @Test
        @WithAnonymousUser
        void getCompanies_WithoutAuth_Returns401() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockJwtAuth
            // no roles
        void getCompanies_WithNoRoles_Returns403() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockJwtAuth(roles = {"ROLE_app_user"})
        void getCompanies_WithUserRole_Returns200() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockJwtAuth(scopes = {"SCOPE_io.playground.company.read-write"})
        void getCompanies_WithServiceScope_Returns200() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockJwtAuth(scopes = {"wrong.scope"})
        void getCompanies_WithWrongScope_Returns403() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockJwtAuth(roles = {"wrong.role"})
        void getCompanies_WithWrongRole_Returns403() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockJwtAuth(roles = {"ROLE_app_user"}, scopes = {"SCOPE_io.playground.company.read-write"})
        void getCompanies_WithBothRoleAndScope_Returns200() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class Delete {
        @Test
        void deleteCompany_WhenExists_ReturnsNoContent() throws Exception {
            Company company = companyRepository.save(Company.builder()
                    .name("To Delete")
                    .taxId(faker.numerify("##########"))
                    .build());

            mockMvc.perform(delete(BASE_URL + "/{id}", company.getId()))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            assertThat(companyRepository.existsById(company.getId())).isFalse();
        }

        @Test
        void deleteCompany_WithExistingEmployees_ReturnsBadRequest() throws Exception {
            Company company = companyRepository.save(Company.builder()
                    .name("Company with Employees")
                    .taxId(faker.numerify("##########"))
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
                    .salary(randomBigDecimal())
                    .build());

            mockMvc.perform(delete(BASE_URL + "/{id}", company.getId()))
                    .andDo(print())
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
                    .taxId(faker.numerify("##########"))
                    .build());

            Department department = departmentRepository.save(Department.builder()
                    .name("IT")
                    .company(company)
                    .build());

            mockMvc.perform(delete(BASE_URL + "/{id}", company.getId()))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            assertThat(companyRepository.existsById(company.getId())).isFalse();
            assertThat(departmentRepository.existsById(department.getId())).isFalse();
        }
    }

    @Nested
    class FindCompanies {
        @Test
        void whenBothCriteriaProvided_ReturnsMatchingCompanies() throws Exception {
            Company company = createTestCompany();
            List<Department> departments = createTestDepartments(company, 4);
            departments.forEach(dept ->
                    IntStream.range(0, 4).forEach(_ -> createTestEmployee(dept)));

            mockMvc.perform(get("/api/companies/find")
                            .param("minDepartments", "3")
                            .param("minEmployees", "10")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(company.getId()));
        }

        @Test
        void whenOnlyMinDepartments_ReturnsMatchingCompanies() throws Exception {
            Company company1 = createTestCompany();
            createTestDepartments(company1, 5);

            Company company2 = createTestCompany();
            createTestDepartments(company2, 2);

            mockMvc.perform(get("/api/companies/find")
                            .param("minDepartments", "4")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(company1.getId()));
        }

        @Test
        void whenOnlyMinEmployees_ReturnsMatchingCompanies() throws Exception {
            Company company1 = createTestCompany();
            Department dept1 = createTestDepartment(company1);
            IntStream.range(0, 5).forEach(_ -> createTestEmployee(dept1));

            Company company2 = createTestCompany();
            Department dept2 = createTestDepartment(company2);
            IntStream.range(0, 2).forEach(_ -> createTestEmployee(dept2));

            mockMvc.perform(get("/api/companies/find")
                            .param("minEmployees", "4")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(company1.getId()));
        }

        @Test
        void whenNoCriteria_ReturnsAllCompanies() throws Exception {
            companyRepository.deleteAll();
            Company company1 = createTestCompany();
            Company company2 = createTestCompany();

            mockMvc.perform(get("/api/companies/find")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(
                            company1.getId().intValue(),
                            company2.getId().intValue()
                    )));
        }

        @Test
        void whenNoResults_ReturnsEmptyPage() throws Exception {
            companyRepository.deleteAll();
            mockMvc.perform(get("/api/companies/find")
                            .param("minDepartments", "1")
                            .param("minEmployees", "1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.page").exists())
                    .andExpect(jsonPath("$.page.totalElements").value(0));
        }
    }
}
