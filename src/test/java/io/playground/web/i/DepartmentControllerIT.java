package io.playground.web.i;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playground.common.AbstractPostgreSQLIntegrationTest;
import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.repository.CompanyRepository;
import io.playground.repository.DepartmentRepository;
import io.playground.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
class DepartmentControllerIT extends AbstractPostgreSQLIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    private Company testCompany;
    private static final String BASE_URL = "/api/departments";

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        companyRepository.deleteAll();
        
        testCompany = companyRepository.save(
            Company.builder()
                .name("Test Company")
                .taxId("1234567890")
                .build()
        );
    }

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
    void createDepartment_InvalidCompanyId_ReturnsBadRequest() throws Exception {
        DepartmentIn departmentIn = DepartmentIn.builder()
            .name("Engineering")
            .companyId(99999L)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(departmentIn)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> 
                    assertThat(result.getResponse().getContentAsString())
                        .contains("Company not found"));
    }

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
    void getDepartment_NonExistentId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> 
                    assertThat(result.getResponse().getContentAsString())
                        .contains("Department not found"));
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

    @Test
    void listDepartmentsByCompany_CompanyNotFound_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/company/{companyId}", 99999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                    assertThat(result.getResponse().getContentAsString())
                        .contains("Company not found"));
    }

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
        Department department = departmentRepository.findById(created.getId()).get();
        Employee employee = Employee.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .department(department)
            .hireDate(Instant.now())
            .build();
        Employee savedEmployee = employeeRepository.save(employee);

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
        Department department1 = departmentRepository.findById(created1.getId()).get();
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
        
        Employee transferredEmployee = employeeRepository.findById(savedEmployee.getId()).get();
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
        Department department1 = departmentRepository.findById(created1.getId()).get();
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
        Department department = departmentRepository.findById(created.getId()).get();
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
                .andExpect(status().isBadRequest());
    }
}