package io.playground.web.it;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static io.playground.helper.NumberUtils.randomBigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
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
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").value("First Corp"))
                    .andExpect(jsonPath("$[1].name").value("Second Corp"));
        }

        @Test
        void getCompany_WithNonexistentId_ReturnsNotFound() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(
                            containsString("Company(999) not found")));
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
}
