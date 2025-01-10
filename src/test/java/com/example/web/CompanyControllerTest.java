package com.example.web;

import com.example.exception.BusinessException;
import com.example.model.CompanyIn;
import com.example.model.CompanyOut;
import com.example.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
//@Import(GlobalExceptionHandler.class)
class CompanyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;  // Spring Boot provides this

    @Test
    void shouldCreateCompany() throws Exception {
        CompanyIn input = new CompanyIn();
        input.setName("Acme");
        input.setTaxId("1234567890");

        CompanyOut output = new CompanyOut();
        output.setId(1L);
        output.setName("Acme");
        output.setTaxId("1234567890");

        when(companyService.create(any(CompanyIn.class))).thenReturn(output);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.taxId").value("1234567890"));
    }

    @Test
    void shouldRejectInvalidCompany() throws Exception {
        CompanyIn input = new CompanyIn();
        // Missing required fields

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Company name is required"))
                .andExpect(jsonPath("$.taxId").value("Tax ID is required"));
    }

    @Test
    void shouldHandleBusinessException() throws Exception {
        CompanyIn input = new CompanyIn();
        input.setName("Acme");
        input.setTaxId("1234567890");

        when(companyService.create(any(CompanyIn.class)))
                .thenThrow(new BusinessException("Tax ID already exists"));

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tax ID already exists"));
    }

    @Test
    void shouldGetCompanyById() throws Exception {
        CompanyOut output = new CompanyOut();
        output.setId(1L);
        output.setName("Acme");
        output.setTaxId("1234567890");

        when(companyService.getById(1L)).thenReturn(output);

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Acme"))
                .andExpect(jsonPath("$.taxId").value("1234567890"));
    }

    @Test
    void shouldHandleCompanyNotFound() throws Exception {
        when(companyService.getById(1L))
                .thenThrow(new BusinessException("Company not found: 1"));

        mockMvc.perform(get("/api/companies/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 1"));
    }

    @Test
    void shouldGetAllCompanies() throws Exception {
        CompanyOut company1 = new CompanyOut();
        company1.setId(1L);
        company1.setName("Acme");

        CompanyOut company2 = new CompanyOut();
        company2.setId(2L);
        company2.setName("Other");

        when(companyService.getAll()).thenReturn(List.of(company1, company2));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldDeleteCompany() throws Exception {
        doNothing().when(companyService).delete(1L);

        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldHandleDeleteNonExistentCompany() throws Exception {
        doThrow(new BusinessException("Company not found: 1"))
                .when(companyService).delete(1L);

        mockMvc.perform(delete("/api/companies/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 1"));
    }
}