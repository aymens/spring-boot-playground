package io.playground.mapper;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import io.playground.model.*;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        CompanyMapperImpl.class,
        DepartmentMapperImpl.class,
        EmployeeMapperImpl.class
})
class MapperTests {
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Nested
    class CompanyMapperTests {
        @Test
        void map_WithNullInput_ReturnsNull() {
            assertThat(companyMapper.map((CompanyIn) null)).isNull();
        }

        @Test
        void map_WithValidCompany_ReturnsExpectedDomain() {
            CompanyIn in = CompanyIn.builder()
                    .name("Acme Corp")
                    .taxId("1234567890").build();

            Company company = companyMapper.map(in);

            assertThat(company.getId()).isNull();
            assertThat(company.getName()).isEqualTo("Acme Corp");
            assertThat(company.getTaxId()).isEqualTo("1234567890");
            assertThat(company.getCreatedAt()).isNull();  // Set by @PrePersist
        }

        @Test
        void map_WithValidDomain_ReturnsExpectedDto() {
            Company company = Company.builder()
                    .id(1L)
                    .name("Acme Corp")
                    .taxId("1234567890")
                    .createdAt(Instant.now())
                    .build();

            CompanyOut out = companyMapper.map(company);

            assertThat(out.getId()).isEqualTo(company.getId());
            assertThat(out.getName()).isEqualTo(company.getName());
            assertThat(out.getTaxId()).isEqualTo(company.getTaxId());
            assertThat(out.getCreatedAt()).isEqualTo(company.getCreatedAt());
        }
    }

    @Nested
    class DepartmentMapperTests {
        @Test
        void map_WithNullInput_ReturnsNull() {
            assertThat(departmentMapper.map(null, null)).isNull();
            assertThat(departmentMapper.map(DepartmentIn.builder().build(), null)).isNull();
            assertThat(departmentMapper.map(null, new Company())).isNull();
        }

        @Test
        void map_WithValidDepartment_ReturnsExpectedDomain() {
            val in = DepartmentIn.builder()
                    .name("IT").build();

            Company company = new Company();
            company.setId(1L);

            Department dept = departmentMapper.map(in, company);

            assertThat(dept.getId()).isNull();
            assertThat(dept.getName()).isEqualTo("IT");
            assertThat(dept.getCompany()).isSameAs(company);
        }

        @Test
        void map_WithValidDepartmentDomain_ReturnsExpectedDto() {
            Company company = Company.builder()
                    .id(1L)
                    .build();

            Department dept = Department.builder()
                    .id(2L)
                    .name("IT")
                    .company(company)
                    .build();

            DepartmentOut out = departmentMapper.map(dept);

            assertThat(out.getId()).isEqualTo(dept.getId());
            assertThat(out.getName()).isEqualTo(dept.getName());
            assertThat(out.getCompanyId()).isEqualTo(company.getId());
        }
    }

    @Nested
    class EmployeeMapperTests {
        @Test
        void map_WithNullValues_ReturnsNull() {
            assertThat(employeeMapper.map(null, null)).isNull();
            assertThat(employeeMapper.map(EmployeeIn.builder().build(), null)).isNull();
            assertThat(employeeMapper.map(null, new Department())).isNull();
        }

        @Test
        void map_WithValidEmployee_ReturnsExpectedDomain () {
            val in = EmployeeIn.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .hireDate(Instant.now())
                    .build();

            Department dept = new Department();
            dept.setId(1L);

            Employee employee = employeeMapper.map(in, dept);

            assertThat(employee.getId()).isNull();
            assertThat(employee.getFirstName()).isEqualTo("John");
            assertThat(employee.getLastName()).isEqualTo("Doe");
            assertThat(employee.getEmail()).isEqualTo("john@example.com");
            assertThat(employee.getHireDate()).isEqualTo(in.getHireDate());
            assertThat(employee.getDepartment()).isSameAs(dept);
        }

        @Test
        void map_WithValidEmployeeDomain_ReturnsExpectedDto() {
            Department dept = new Department();
            dept.setId(1L);

            Employee employee = Employee.builder()
                    .id(2L)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@example.com")
                    .hireDate(Instant.now())
                    .department(dept)
                    .build();

            EmployeeOut out = employeeMapper.map(employee);

            assertThat(out.getId()).isEqualTo(employee.getId());
            assertThat(out.getFirstName()).isEqualTo(employee.getFirstName());
            assertThat(out.getLastName()).isEqualTo(employee.getLastName());
            assertThat(out.getEmail()).isEqualTo(employee.getEmail());
            assertThat(out.getHireDate()).isEqualTo(employee.getHireDate());
            assertThat(out.getDepartmentId()).isEqualTo(dept.getId());
        }
    }
}