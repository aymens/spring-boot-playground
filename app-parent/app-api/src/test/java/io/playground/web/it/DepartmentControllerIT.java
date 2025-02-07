package io.playground.web.it;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class DepartmentControllerIT extends BaseControllerIntegrationTest {

    private static final String BASE_URL = "/api/departments";

    @Nested
    class Create {
        @Test
        void createDepartment_ValidInput_ReturnsCreated() throws Exception {
            DepartmentIn departmentIn = DepartmentIn.builder()
                    .name("Engineering")
                    .companyId(testCompany.getId())
                    .build();

            MvcResult result = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentIn)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            assertThat(created)
                    .isNotNull()
                    .satisfies(department -> {
                        assertThat(department.getId()).isNotNull();
                        assertThat(department.getName()).isEqualTo(departmentIn.getName());
                        assertThat(department.getCompanyId()).isEqualTo(testCompany.getId());
                    });
        }

        @Test
        void createDepartment_InvalidCompanyId_ReturnsNotFound() throws Exception {
            DepartmentIn departmentIn = DepartmentIn.builder()
                    .name("Engineering")
                    .companyId(99999L)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentIn)))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .endsWith("Company(99999) not found"));
        }

        @Test
        void createDepartment_DuplicateNameInCompany_ReturnsBadRequest() throws Exception {
            // First create a department
            DepartmentIn departmentIn = DepartmentIn.builder()
                    .name("IT")
                    .companyId(testCompany.getId())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentIn)))
                    .andExpect(status().isOk());

            // Try to create another with same name
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentIn)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Department already exists in company"));
        }

        @Test
        void createDepartment_InvalidInput_ReturnsBadRequest() throws Exception {
            // Empty name
            DepartmentIn emptyName = DepartmentIn.builder()
                    .name("")
                    .companyId(testCompany.getId())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyName)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Department name is required"));

            // Null companyId
            DepartmentIn nullCompany = DepartmentIn.builder()
                    .name("IT")
                    .companyId(null)
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nullCompany)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Company ID is required"));
        }

        @Test
        void createDepartment_NameTooLong_ReturnsBadRequest() throws Exception {
            DepartmentIn longName = DepartmentIn.builder()
                    .name("A".repeat(51))  // > 50 chars
                    .companyId(testCompany.getId())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(longName)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Department name cannot exceed 50 characters"));
        }

        @Test
        void createDepartment_SameNameDifferentCompanies_Succeeds() throws Exception {
            // Create second company
            Company secondCompany = companyRepository.save(
                    Company.builder()
                            .name("Second Company")
                            .taxId("9876543210")
                            .build()
            );

            // Create department in first company
            DepartmentIn dept1 = DepartmentIn.builder()
                    .name("IT")
                    .companyId(testCompany.getId())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept1)))
                    .andExpect(status().isOk());

            // Create department with same name in second company
            DepartmentIn dept2 = DepartmentIn.builder()
                    .name("IT")
                    .companyId(secondCompany.getId())
                    .build();

            MvcResult result = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept2)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            assertThat(created)
                    .isNotNull()
                    .satisfies(department -> {
                        assertThat(department.getName()).isEqualTo("IT");
                        assertThat(department.getCompanyId()).isEqualTo(secondCompany.getId());
                    });
        }
    }

    @Nested
    class Get {
        @Test
        void getDepartment_ExistingId_ReturnsOk() throws Exception {
            // First create a department
            DepartmentIn departmentIn = DepartmentIn.builder()
                    .name("HR")
                    .companyId(testCompany.getId())
                    .build();

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentIn)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            // Then retrieve it
            MvcResult getResult = mockMvc.perform(get(BASE_URL + "/{id}", created.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut retrieved = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            assertThat(retrieved)
                    .isNotNull()
                    .satisfies(department -> {
                        assertThat(department.getId()).isEqualTo(created.getId());
                        assertThat(department.getName()).isEqualTo(created.getName());
                        assertThat(department.getCompanyId()).isEqualTo(created.getCompanyId());
                    });
        }

        @Test
        void listDepartmentsByCompany_ValidCompanyId_ReturnsOk() throws Exception {
            departmentRepository.deleteAll();
            // Create multiple departments for the test company
            DepartmentIn dept1 = DepartmentIn.builder()
                    .name("HR")
                    .companyId(testCompany.getId())
                    .build();

            DepartmentIn dept2 = DepartmentIn.builder()
                    .name("IT")
                    .companyId(testCompany.getId())
                    .build();

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept1)))
                    .andExpect(status().isOk());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept2)))
                    .andExpect(status().isOk());

            // List departments by company
            MvcResult listResult = mockMvc.perform(get(BASE_URL + "/company/{companyId}", testCompany.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            List<DepartmentOut> departments = objectMapper.readValue(
                    listResult.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, DepartmentOut.class)
            );

            assertThat(departments)
                    .hasSize(2)
                    .extracting(DepartmentOut::getName)
                    .containsExactlyInAnyOrder("HR", "IT");

            assertThat(departments)
                    .allSatisfy(department ->
                            assertThat(department.getCompanyId()).isEqualTo(testCompany.getId())
                    );
        }

        @Test
        void getDepartment_NonExistentId_ReturnsNotFound() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Department(99999) not found"));
        }

        @Test
        void listDepartmentsByCompany_EmptyResult_ReturnsOk() throws Exception {
            // Create a new company without departments
            Company emptyCompany = companyRepository.save(
                    Company.builder()
                            .name("Empty Company")
                            .taxId("9876543210")
                            .build()
            );

            MvcResult listResult = mockMvc.perform(get(BASE_URL + "/company/{companyId}", emptyCompany.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            List<DepartmentOut> departments = objectMapper.readValue(
                    listResult.getResponse().getContentAsString(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, DepartmentOut.class)
            );

            assertThat(departments).isEmpty();
        }

        @Test
        void listDepartmentsByCompany_CompanyNotFound_ReturnsBadRequest() throws Exception {
            mockMvc.perform(get(BASE_URL + "/company/{companyId}", 99999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Company(99999) not found"));
        }

        @Test
        void find_WithAllFiltersAndPagination_ReturnsFilteredPagedResults() throws Exception {
            // Given
            var department1 = createTestDepartment(testCompany);
            var department2 = createTestDepartment(testCompany);
            createTestEmployee(department1);
            createTestEmployee(department1);
            createTestEmployee(department2);

            // When/Then
            mockMvc.perform(get("/api/departments/search")
                            .param("companyId", testCompany.getId().toString())
                            .param("minEmployees", "2")
                            .param("nameFilter", department1.getName().substring(0, 3))
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(department1.getId()))
                    .andExpect(jsonPath("$.content[0].name").value(department1.getName()))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        void find_WithDefaultParameters_ReturnsAllDepartmentsWithDefaultPaging() throws Exception {
            departmentRepository.deleteAll();
            // Given
            createTestDepartments(testCompany, 3);

            // When/Then
            mockMvc.perform(get("/api/departments/search"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements").value(3));
        }

        @Test
        void find_WithNonExistentCompanyId_ReturnsEmptyPage() throws Exception {
            // Given
            if (companyRepository.existsById(99999L)) {
                companyRepository.deleteById(99999L);
            }

            // When/Then
            mockMvc.perform(get("/api/departments/search")
                            .param("companyId", "99999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    class Delete {
        @Test
        void deleteDepartment_WithEmployees_RequiresTransferDepartment() throws Exception {
            // Create first department (to be deleted)
            DepartmentIn dept1 = DepartmentIn.builder()
                    .name("IT")
                    .companyId(testCompany.getId())
                    .build();

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept1)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            // Add an employee to the department
            Department department = departmentRepository.findById(created.getId()).orElseThrow();

            Employee employee = Employee.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .department(department)
                    .hireDate(Instant.now())
                    .build();
            employeeRepository.save(employee);

            // Try to delete without transfer department
            mockMvc.perform(delete(BASE_URL + "/{id}", created.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Must specify a transfer department ID"));
        }

        @Test
        void deleteDepartment_WithEmployees_TransfersSuccessfully() throws Exception {
            // Create first department (to be deleted)
            DepartmentIn dept1 = DepartmentIn.builder()
                    .name("IT")
                    .companyId(testCompany.getId())
                    .build();

            MvcResult createResult1 = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept1)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created1 = objectMapper.readValue(
                    createResult1.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            // Create second department (transfer target)
            DepartmentIn dept2 = DepartmentIn.builder()
                    .name("Engineering")
                    .companyId(testCompany.getId())
                    .build();

            MvcResult createResult2 = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept2)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created2 = objectMapper.readValue(
                    createResult2.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            // Add an employee to the first department
            Department department1 = departmentRepository.findById(created1.getId()).orElseThrow();

            Employee employee = Employee.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .department(department1)
                    .hireDate(Instant.now())
                    .build();
            Employee savedEmployee = employeeRepository.save(employee);

            // Delete first department with transfer
            mockMvc.perform(delete(BASE_URL + "/{id}?transferToId={transferId}",
                            created1.getId(), created2.getId()))
                    .andExpect(status().isNoContent());

            // Verify
            assertThat(departmentRepository.existsById(created1.getId())).isFalse();
            assertThat(employeeRepository.existsById(savedEmployee.getId())).isTrue();

            Employee transferredEmployee = employeeRepository.findById(savedEmployee.getId()).orElseThrow();

            assertThat(transferredEmployee.getDepartment().getId()).isEqualTo(created2.getId());
        }

        @Test
        void deleteDepartment_WithEmployees_DifferentCompany_ReturnsBadRequest() throws Exception {
            // Create department in first company
            DepartmentIn dept1 = DepartmentIn.builder()
                    .name("IT")
                    .companyId(testCompany.getId())
                    .build();

            MvcResult createResult1 = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept1)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created1 = objectMapper.readValue(
                    createResult1.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            // Create second company and department
            Company company2 = companyRepository.save(Company.builder()
                    .name("Second Company")
                    .taxId("9876543210")
                    .build());

            DepartmentIn dept2 = DepartmentIn.builder()
                    .name("Engineering")
                    .companyId(company2.getId())
                    .build();

            MvcResult createResult2 = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept2)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created2 = objectMapper.readValue(
                    createResult2.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            // Add an employee to the first department
            Department department1 = departmentRepository.findById(created1.getId()).orElseThrow();

            Employee employee = Employee.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .department(department1)
                    .hireDate(Instant.now())
                    .build();
            employeeRepository.save(employee);

            // Try to transfer to department in different company
            mockMvc.perform(delete(BASE_URL + "/{id}?transferToId={transferId}",
                            created1.getId(), created2.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Target department must be in the same company"));
        }

        @Test
        void deleteDepartment_WithEmployees_SelfTransfer_ReturnsBadRequest() throws Exception {
            // Create department
            DepartmentIn dept = DepartmentIn.builder()
                    .name("IT")
                    .companyId(testCompany.getId())
                    .build();

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dept)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            // Add an employee
            Department department = departmentRepository.findById(created.getId()).orElseThrow();

            Employee employee = Employee.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .department(department)
                    .hireDate(Instant.now())
                    .build();
            employeeRepository.save(employee);

            // Try to transfer to self
            mockMvc.perform(delete(BASE_URL + "/{id}?transferToId={transferId}",
                            created.getId(), created.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(result ->
                            assertThat(result.getResponse().getContentAsString())
                                    .contains("Cannot transfer employees to the same department"));
        }

        @Test
        void deleteDepartment_ExistingId_ReturnsNoContent() throws Exception {
            // First create a department
            DepartmentIn departmentIn = DepartmentIn.builder()
                    .name("Finance")
                    .companyId(testCompany.getId())
                    .build();

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(departmentIn)))
                    .andExpect(status().isOk())
                    .andReturn();

            DepartmentOut created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    DepartmentOut.class
            );

            // Delete the department
            mockMvc.perform(delete(BASE_URL + "/{id}", created.getId()))
                    .andExpect(status().isNoContent());

            // Verify it's deleted by trying to get it
            mockMvc.perform(get(BASE_URL + "/{id}", created.getId()))
                    .andExpect(status().isNotFound());
        }
    }
}