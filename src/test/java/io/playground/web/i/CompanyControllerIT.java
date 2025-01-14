package io.playground.web.i;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.common.AbstractPostgreSQLIntegrationTest;
import io.playground.domain.Company;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
class CompanyControllerIT extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void contextLoads() {
        assertThat(POSTGRES.isRunning()).isTrue();
    }

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveCompany() throws Exception {
        // Given
        CompanyIn input = CompanyIn.builder()
                .name("Acme Corp")
                .taxId("1234567890")
                .build();

        // When - Create
        MvcResult createResult = mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andExpect(jsonPath("$.taxId").value("1234567890"))
                .andReturn();

        CompanyOut created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CompanyOut.class);

        // Then - Get by ID
        String getUri = UriComponentsBuilder
                .fromPath("/api/companies/{id}")
                .buildAndExpand(created.getId())
                .toUriString();

        mockMvc.perform(get(getUri))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andExpect(jsonPath("$.taxId").value("1234567890"));
    }

    @Test
    void shouldReturnAllCompanies() throws Exception {
        // Given
        companyRepository.saveAll(List.of(
                Company.builder().name("First Corp").taxId("1111111111").build(),
                Company.builder().name("Second Corp").taxId("2222222222").build()
        ));

        // When
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("First Corp"))
                .andExpect(jsonPath("$[1].name").value("Second Corp"));
    }

    @Test
    void shouldHandleEmptyCompanyList() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldPreventDuplicateTaxId() throws Exception {
        // Given
        String duplicateTaxId = "1234567890";
        companyRepository.save(Company.builder()
                .name("First Corp")
                .taxId(duplicateTaxId)
                .build());

        CompanyIn input = CompanyIn.builder()
                .name("Second Corp")
                .taxId(duplicateTaxId)
                .build();

        // When/Then
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company with tax ID already exists: " + duplicateTaxId));
    }

    @Test
    void shouldDeleteCompany() throws Exception {
        // Given
        Company company = companyRepository.save(Company.builder()
                .name("To Delete")
                .taxId("1234567890")
                .build());

        String uri = UriComponentsBuilder
                .fromPath("/api/companies/{id}")
                .buildAndExpand(company.getId())
                .toUriString();

        // When
        mockMvc.perform(delete(uri))
                .andExpect(status().isNoContent());

        // Then
        mockMvc.perform(get(uri))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: " + company.getId()));

        assertThat(companyRepository.existsById(company.getId())).isFalse();
    }

    @Test
    void shouldHandleCompanyNotFound() throws Exception {
        String uri = UriComponentsBuilder
                .fromPath("/api/companies/{id}")
                .buildAndExpand(999L)
                .toUriString();

        mockMvc.perform(get(uri))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 999"));
    }

    @Test
    void shouldRejectInvalidCompanyData() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("")  // Empty name
                .taxId("123")  // Invalid tax ID
                .build();

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Company name is required"))
                .andExpect(jsonPath("$.taxId").value("Tax ID must be exactly 10 digits"));
    }

}
