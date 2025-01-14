package io.playground.web.u;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.exception.BusinessException;
import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import io.playground.service.EmployeeService;
import io.playground.web.EmployeeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateEmployee() throws Exception {
        EmployeeIn input = EmployeeIn.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .departmentId(1L)
                .hireDate(Instant.now())
                .build();

        EmployeeOut output = EmployeeOut.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .departmentId(1L)
                .hireDate(input.getHireDate())
                .build();

        when(employeeService.create(any(EmployeeIn.class))).thenReturn(output);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldRejectInvalidEmployee() throws Exception {
        EmployeeIn input = EmployeeIn.builder().build();// Missing all required fields

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name is required"))
                .andExpect(jsonPath("$.lastName").value("Last name is required"))
                .andExpect(jsonPath("$.email").value("Email is required"))
                .andExpect(jsonPath("$.departmentId").value("Department ID is required"))
                .andExpect(jsonPath("$.hireDate").value("Hire date is required"));
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {
        EmployeeIn input = EmployeeIn.builder()
                .firstName("John")
                .lastName("Doe")
                .email("not-an-email")  // Invalid email format
                .departmentId(1L)
                .hireDate(Instant.now())
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Invalid email format"));
    }

    @Test
    void shouldRejectFutureHireDate() throws Exception {
        EmployeeIn input = EmployeeIn.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .departmentId(1L)
                .hireDate(Instant.now().plus(1, ChronoUnit.DAYS))  // Future date
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.hireDate").value("Hire date cannot be in the future"));
    }

    @Test
    void shouldHandleDuplicateEmail() throws Exception {
        EmployeeIn input = EmployeeIn.builder()
                .firstName("John")
                .lastName("Doe")
                .email("existing@example.com")
                .departmentId(1L)
                .hireDate(Instant.now())
                .build();

        when(employeeService.create(any(EmployeeIn.class)))
                .thenThrow(new BusinessException("Employee with email already exists"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Employee with email already exists"));
    }

    @Test
    void shouldHandleDepartmentNotFound() throws Exception {
        EmployeeIn input = EmployeeIn.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .departmentId(999L)
                .hireDate(Instant.now())
                .build();

        when(employeeService.create(any(EmployeeIn.class)))
                .thenThrow(new BusinessException("Department not found: 999"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Department not found: 999"));
    }

    @Test
    void shouldGetEmployeeById() throws Exception {
        EmployeeOut output = EmployeeOut.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .departmentId(1L)
                .hireDate(Instant.now())
                .build();

        when(employeeService.getById(1L)).thenReturn(output);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldGetEmployeesByDepartment() throws Exception {
        EmployeeOut employee1 = EmployeeOut.builder()
                .id(1L)
                .firstName("John")
                .build();

        EmployeeOut employee2 = EmployeeOut.builder()
                .id(2L)
                .firstName("Jane")
                .build();

        when(employeeService.getByDepartmentId(1L)).thenReturn(List.of(employee1, employee2));

        mockMvc.perform(get("/api/employees/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void shouldReturnEmptyDepartmentEmployeesList() throws Exception {
        when(employeeService.getByDepartmentId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employees/department/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        doNothing().when(employeeService).delete(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldHandleDeleteNonExistentEmployee() throws Exception {
        doThrow(new BusinessException("Employee not found: 999"))
                .when(employeeService).delete(999L);

        mockMvc.perform(delete("/api/employees/999"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Employee not found: 999"));
    }
}