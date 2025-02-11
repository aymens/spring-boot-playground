package io.playground.web.ut;

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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static io.playground.test.data.PageUtils.pageOf;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    void createDepartment_WithValidInput_ReturnsCreated() throws Exception {
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
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"))
                .andExpect(jsonPath("$.companyId").value(1));
    }

    @Test
    void createDepartment_WithInvalidInput_ReturnsBadRequest() throws Exception {
        DepartmentIn input = DepartmentIn.builder().build();// Missing required fields

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Department name is required"))
                .andExpect(jsonPath("$.companyId").value("Company ID is required"));
    }

    @Test
    void createDepartment_WithDuplicateName_ReturnsBadRequest() throws Exception {
        DepartmentIn input = DepartmentIn.builder()
                .name("IT")
                .companyId(1L)
                .build();

        when(departmentService.create(any(DepartmentIn.class)))
                .thenThrow(new BusinessException("Department IT already exists in company 1"));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department IT already exists in company 1"));
    }

    @Test
    void createDepartment_WithInvalidCompany_ReturnsNotFound() throws Exception {
        DepartmentIn input = DepartmentIn.builder()
                .name("IT")
                .companyId(999L)
                .build();

        when(departmentService.create(any(DepartmentIn.class)))
                .thenThrow(new BusinessException("Company not found: 999"));

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 999"));
    }

    @Test
    void getDepartment_WithValidId_ReturnsDepartment() throws Exception {
        DepartmentOut output = DepartmentOut.builder()
                .id(1L)
                .name("IT")
                .companyId(1L)
                .build();

        when(departmentService.getById(1L)).thenReturn(output);

        mockMvc.perform(get("/api/departments/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("IT"))
                .andExpect(jsonPath("$.companyId").value(1));
    }

    @Test
    void getDepartment_WithInvalidId_ReturnsBadRequest() throws Exception {
        when(departmentService.getById(999L))
                .thenThrow(new BusinessException("Department not found: 999"));

        mockMvc.perform(get("/api/departments/999"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department not found: 999"));
    }

    @Test
    void getDepartments_ByCompanyId_ReturnsList() throws Exception {
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

        when(departmentService.getByCompanyId(eq(1L), any(Pageable.class))).thenReturn(pageOf(dept1, dept2));

        mockMvc.perform(get("/api/departments/company/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("IT"))
                .andExpect(jsonPath("$.content[1].name").value("HR"));
    }

    @Test
    void getDepartments_WithInvalidCompany_ReturnsBadRequest() throws Exception {
        when(departmentService.getByCompanyId(eq(999L), any(Pageable.class)))
                .thenThrow(new BusinessException("Company not found: 999"));

        mockMvc.perform(get("/api/departments/company/999"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Company not found: 999"));
    }

    @Test
    void deleteDepartment_WhenExists_ReturnsNoContent() throws Exception {
        doNothing().when(departmentService).delete(eq(1L), isNull());

        mockMvc.perform(delete("/api/departments/1"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(departmentService).delete(1L, null);
    }

    @Test
    void deleteDepartment_WithInvalidId_ReturnsBadRequest() throws Exception {
        doThrow(new BusinessException("Department not found: 999"))
                .when(departmentService).delete(eq(999L), isNull());

        mockMvc.perform(delete("/api/departments/999"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department not found: 999"));
    }
}