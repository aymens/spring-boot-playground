package io.playground.web.it;

import io.playground.domain.Employee;
import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class EmployeeControllerIT extends BaseControllerIntegrationTest {

    private static final String BASE_URL = "/api/employees";

    @Nested
    class Create {
        @Test
        void createEmployee_WithValidInput_ReturnsCreated() throws Exception {
            EmployeeIn employeeIn = EmployeeIn.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .departmentId(testDepartment.getId())
                    .hireDate(Instant.now())
                    .build();

            MvcResult result = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeIn)))
                    .andExpect(status().isOk())
                    .andReturn();

            EmployeeOut created = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    EmployeeOut.class
            );

            assertThat(created)
                    .isNotNull()
                    .satisfies(employee -> {
                        assertThat(employee.getId()).isNotNull().isPositive();
                        assertThat(employee.getFirstName()).isEqualTo(employeeIn.getFirstName());
                        assertThat(employee.getLastName()).isEqualTo(employeeIn.getLastName());
                        assertThat(employee.getEmail()).isEqualTo(employeeIn.getEmail());
                        assertThat(employee.getDepartmentId()).isEqualTo(testDepartment.getId());
                    });

            // Verify in DB
            Employee savedEmployee = employeeRepository.findById(created.getId()).orElseThrow();
            assertThat(savedEmployee.getEmail()).isEqualTo(employeeIn.getEmail());
        }

        @Test
        void createEmployee_WithInvalidDepartment_ReturnsBadRequest() throws Exception {
            EmployeeIn employeeIn = EmployeeIn.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .departmentId(999L)
                    .hireDate(Instant.now())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeIn)))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Department(999) not found"));
        }

        @Test
        void createEmployee_WithDuplicateEmail_ReturnsBadRequest() throws Exception {
            // First create an employee
            Employee existingEmployee = employeeRepository.save(
                    Employee.builder()
                            .firstName("Jane")
                            .lastName("Smith")
                            .email("jane.smith@example.com")
                            .department(testDepartment)
                            .hireDate(Instant.now())
                            .build()
            );

            // Try to create another with same email
            EmployeeIn employeeIn = EmployeeIn.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email(existingEmployee.getEmail())
                    .departmentId(testDepartment.getId())
                    .hireDate(Instant.now())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeIn)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Employee with email already exists"));
        }

        @Test
        void createEmployee_WithInvalidEmail_ReturnsBadRequest() throws Exception {
            EmployeeIn employeeIn = EmployeeIn.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("not-an-email")
                    .departmentId(testDepartment.getId())
                    .hireDate(Instant.now())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeIn)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.email").value("Invalid email format"));
        }

        @Test
        void createEmployee_WithMissingFields_ReturnsBadRequest() throws Exception {
            EmployeeIn employeeIn = EmployeeIn.builder().build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeIn)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.firstName").value("First name is required"))
                    .andExpect(jsonPath("$.lastName").value("Last name is required"))
                    .andExpect(jsonPath("$.email").value("Email is required"))
                    .andExpect(jsonPath("$.departmentId").value("Department ID is required"))
                    .andExpect(jsonPath("$.hireDate").value("Hire date is required"));
        }

        @Test
        void createEmployee_WithFutureDate_ReturnsBadRequest() throws Exception {
            EmployeeIn employeeIn = EmployeeIn.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .departmentId(testDepartment.getId())
                    .hireDate(Instant.now().plus(Duration.ofDays(1)))
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeIn)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.hireDate").value("Hire date cannot be in the future"));
        }
    }

    @Nested
    class Get {
        @Test
        void getEmployee_WithValidId_ReturnsEmployee() throws Exception {
            Employee employee = employeeRepository.save(
                    Employee.builder()
                            .firstName("John")
                            .lastName("Doe")
                            .email("john.doe@example.com")
                            .department(testDepartment)
                            .hireDate(Instant.now())
                            .build()
            );

            MvcResult result = mockMvc.perform(get(BASE_URL + "/{id}", employee.getId()))
                    .andExpect(status().isOk())
                    .andReturn();

            EmployeeOut retrieved = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    EmployeeOut.class
            );

            assertThat(retrieved)
                    .isNotNull()
                    .satisfies(emp -> {
                        assertThat(emp.getId()).isEqualTo(employee.getId());
                        assertThat(emp.getFirstName()).isEqualTo(employee.getFirstName());
                        assertThat(emp.getLastName()).isEqualTo(employee.getLastName());
                        assertThat(emp.getEmail()).isEqualTo(employee.getEmail());
                        assertThat(emp.getDepartmentId()).isEqualTo(testDepartment.getId());
                    });
        }

        @Test
        void getEmployee_WithInvalidId_ReturnsNotFound() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Employee(999) not found"));
        }

        @Test
        void getEmployees_ByDepartmentId_ReturnsList() throws Exception {
            // Create multiple employees in the department
            employeeRepository.save(
                    Employee.builder()
                            .firstName("John")
                            .lastName("Doe")
                            .email("john.doe@example.com")
                            .department(testDepartment)
                            .hireDate(Instant.now())
                            .build()
            );

            employeeRepository.save(
                    Employee.builder()
                            .firstName("Jane")
                            .lastName("Smith")
                            .email("jane.smith@example.com")
                            .department(testDepartment)
                            .hireDate(Instant.now())
                            .build()
            );

            MvcResult result = mockMvc.perform(get(BASE_URL + "/department/{departmentId}", testDepartment.getId()))
                    .andExpect(status().isOk())
                    .andReturn();

            List<EmployeeOut> employees = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, EmployeeOut.class)
            );

            assertThat(employees)
                    .hasSize(2)
                    .extracting(EmployeeOut::getDepartmentId)
                    .containsOnly(testDepartment.getId());
        }

        @Test
        void getEmployees_WithInvalidDepartmentId_ReturnsBadRequest() throws Exception {
            mockMvc.perform(get(BASE_URL + "/department/{departmentId}", 999L))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Department not found: 999"));
        }
    }

    @Nested
    class Delete {
        @Test
        void deleteEmployee_WhenExists_ReturnsNoContent() throws Exception {
            Employee employee = employeeRepository.save(
                    Employee.builder()
                            .firstName("John")
                            .lastName("Doe")
                            .email("john.doe@example.com")
                            .department(testDepartment)
                            .hireDate(Instant.now())
                            .build()
            );

            mockMvc.perform(delete(BASE_URL + "/{id}", employee.getId()))
                    .andExpect(status().isNoContent());

            assertThat(employeeRepository.existsById(employee.getId())).isFalse();
        }

        @Test
        void deleteEmployee_WithInvalidId_ReturnsNotFound() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Employee(999) not found"));
        }
    }
}