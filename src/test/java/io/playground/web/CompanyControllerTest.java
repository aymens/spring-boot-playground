package io.playground.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.exception.BusinessException;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.service.CompanyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateCompany() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("Acme")
                .taxId("1234567890")
                .build();

        CompanyOut output = CompanyOut.builder()
                .id(1L)
                .name("Acme")
                .taxId("1234567890")
                .build();


        when(companyService.create(any(CompanyIn.class))).thenReturn(output);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())  // Changed to 201
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.taxId").value("1234567890"));
    }

    @Test
    void shouldRejectMissingFields() throws Exception {
        CompanyIn input = CompanyIn.builder().build();// Missing all required fields

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Company name is required"))
                .andExpect(jsonPath("$.taxId").value("Tax ID is required"));
    }

    @Test
    void shouldRejectTooLongName() throws Exception {
        CompanyIn input = CompanyIn.builder().build();
        input.setName("A".repeat(101));  // > 100 chars
        input.setTaxId("1234567890");

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Company name cannot exceed 100 characters"));
    }

    @Test
    void shouldRejectInvalidTaxIdFormat() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("Acme")
                .taxId("123") // Not 10 digits
                .build();

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.taxId").value("Tax ID must be exactly 10 digits"));
    }

    @Test
    void shouldRejectNonNumericTaxId() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("Acme")
                .taxId("123ABC4567")
                .build();


        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.taxId").value("Tax ID must be exactly 10 digits"));
    }

    @Test
    void shouldHandleDuplicateTaxId() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("Acme")
                .taxId("1234567890")
                .build();

        when(companyService.create(any(CompanyIn.class)))
                .thenThrow(new BusinessException("Company with tax ID already exists: 1234567890"));

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company with tax ID already exists: 1234567890"));
    }

    @Test
    void shouldGetCompanyById() throws Exception {
        CompanyOut output = CompanyOut.builder()
                .id(1L)
                .name("Acme")
                .taxId("1234567890")
                .build();


        when(companyService.getById(1L)).thenReturn(output);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.taxId").value("1234567890"));
    }

    @Test
    void shouldRejectNegativeId() throws Exception {
        mockMvc.perform(get("/api/companies/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleCompanyNotFound() throws Exception {
        when(companyService.getById(999L))
                .thenThrow(new BusinessException("Company not found: 999"));

        mockMvc.perform(get("/api/companies/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 999"));
    }

    @Test
    void shouldGetAllCompanies() throws Exception {
        CompanyOut company1 = CompanyOut.builder()
                .id(1L)
                .name("Acme").build();

        CompanyOut company2 = CompanyOut.builder()
                .id(2L)
                .name("Other").build();

        when(companyService.getAll()).thenReturn(List.of(company1, company2));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldReturnEmptyList() throws Exception {
        when(companyService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldDeleteCompany() throws Exception {
        doNothing().when(companyService).delete(1L);

        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isNoContent());

        verify(companyService).delete(1L);
    }

    @Test
    void shouldHandleDeleteNonExistentCompany() throws Exception {
        doThrow(new BusinessException("Company not found: 999"))
                .when(companyService).delete(999L);

        mockMvc.perform(delete("/api/companies/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 999"));
    }

    @Test
    void shouldRejectInvalidContentType() throws Exception {
        CompanyIn input = CompanyIn.builder()
                .name("Acme")
                .taxId("1234567890")
                .build();

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isUnsupportedMediaType());
    }
}
