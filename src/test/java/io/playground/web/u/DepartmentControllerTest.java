package io.playground.web.u;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.exception.BusinessException;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.service.DepartmentService;
import io.playground.web.DepartmentController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentService departmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateDepartment() throws Exception {
        DepartmentIn input = DepartmentIn.builder()
                .name("IT")
                .companyId(1L)
                .build();

        DepartmentOut output = DepartmentOut.builder()
                .id(1L)
                .name("IT")
                .companyId(1L)
                .build();

        when(departmentService.create(any(DepartmentIn.class))).thenReturn(output);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"))
                .andExpect(jsonPath("$.companyId").value(1));
    }

    @Test
    void shouldRejectInvalidDepartment() throws Exception {
        DepartmentIn input = DepartmentIn.builder().build();// Missing required fields

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Department name is required"))
                .andExpect(jsonPath("$.companyId").value("Company ID is required"));
    }

    @Test
    void shouldHandleDepartmentNameDuplicateInCompany() throws Exception {
        DepartmentIn input = DepartmentIn.builder()
                .name("IT")
                .companyId(1L)
                .build();

        when(departmentService.create(any(DepartmentIn.class)))
                .thenThrow(new BusinessException("Department IT already exists in company 1"));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department IT already exists in company 1"));
    }

    @Test
    void shouldHandleCompanyNotFoundOnCreate() throws Exception {
        DepartmentIn input = DepartmentIn.builder()
                .name("IT")
                .companyId(999L)
                .build();

        when(departmentService.create(any(DepartmentIn.class)))
                .thenThrow(new BusinessException("Company not found: 999"));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 999"));
    }

    @Test
    void shouldGetDepartmentById() throws Exception {
        DepartmentOut output = DepartmentOut.builder()
                .id(1L)
                .name("IT")
                .companyId(1L)
                .build();

        when(departmentService.getById(1L)).thenReturn(output);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"))
                .andExpect(jsonPath("$.companyId").value(1));
    }

    @Test
    void shouldHandleDepartmentNotFound() throws Exception {
        when(departmentService.getById(999L))
                .thenThrow(new BusinessException("Department not found: 999"));

        mockMvc.perform(get("/api/departments/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department not found: 999"));
    }

    @Test
    void shouldGetDepartmentsByCompanyId() throws Exception {
        DepartmentOut dept1 = DepartmentOut.builder()
                .id(1L)
                .name("IT")
                .companyId(1L)
                .build();

        DepartmentOut dept2 = DepartmentOut.builder()
                .id(2L)
                .name("HR")
                .companyId(1L)
                .build();

        when(departmentService.getByCompanyId(1L)).thenReturn(List.of(dept1, dept2));

        mockMvc.perform(get("/api/departments/company/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("IT"))
                .andExpect(jsonPath("$[1].name").value("HR"));
    }

    @Test
    void shouldHandleCompanyNotFoundForDepartmentList() throws Exception {
        when(departmentService.getByCompanyId(999L))
                .thenThrow(new BusinessException("Company not found: 999"));

        mockMvc.perform(get("/api/departments/company/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 999"));
    }

    @Test
    void shouldDeleteDepartment() throws Exception {
        doNothing().when(departmentService).delete(eq(1L), isNull());

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isNoContent());

        verify(departmentService).delete(1L, null);
    }

    @Test
    void shouldHandleDeleteNonExistentDepartment() throws Exception {
        doThrow(new BusinessException("Department not found: 999"))
                .when(departmentService).delete(eq(999L), isNull());

        mockMvc.perform(delete("/api/departments/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department not found: 999"));
    }
}