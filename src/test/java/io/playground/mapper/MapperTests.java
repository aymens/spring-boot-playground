package io.playground.mapper;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.domain.Employee;
import com.example.model.*;
import io.playground.model.*;
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
        void shouldHandleNull() {
            assertThat(companyMapper.map((CompanyIn) null)).isNull();
        }

        @Test
        void shouldMapValidCompany() {
            CompanyIn in = new CompanyIn();
            in.setName("Acme Corp");
            in.setTaxId("1234567890");

            Company company = companyMapper.map(in);

            assertThat(company.getId()).isNull();
            assertThat(company.getName()).isEqualTo("Acme Corp");
            assertThat(company.getTaxId()).isEqualTo("1234567890");
            assertThat(company.getCreatedAt()).isNull();  // Set by @PrePersist
        }

        @Test
        void shouldMapToOut() {
            Company company = new Company();
            company.setId(1L);
            company.setName("Acme Corp");
            company.setTaxId("1234567890");
            company.setCreatedAt(Instant.now());

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
        void shouldHandleNulls() {
            assertThat(departmentMapper.map(null, null)).isNull();
            assertThat(departmentMapper.map(new DepartmentIn(), null)).isNull();
            assertThat(departmentMapper.map(null, new Company())).isNull();
        }

        @Test
        void shouldMapValidDepartment() {
            DepartmentIn in = new DepartmentIn();
            in.setName("IT");

            Company company = new Company();
            company.setId(1L);

            Department dept = departmentMapper.map(in, company);

            assertThat(dept.getId()).isNull();
            assertThat(dept.getName()).isEqualTo("IT");
            assertThat(dept.getCompany()).isSameAs(company);
        }

        @Test
        void shouldMapToOut() {
            Company company = new Company();
            company.setId(1L);

            Department dept = new Department();
            dept.setId(2L);
            dept.setName("IT");
            dept.setCompany(company);

            DepartmentOut out = departmentMapper.map(dept);

            assertThat(out.getId()).isEqualTo(dept.getId());
            assertThat(out.getName()).isEqualTo(dept.getName());
            assertThat(out.getCompanyId()).isEqualTo(company.getId());
        }
    }

    @Nested
    class EmployeeMapperTests {
        @Test
        void shouldHandleNulls() {
            assertThat(employeeMapper.map(null, null)).isNull();
            assertThat(employeeMapper.map(new EmployeeIn(), null)).isNull();
            assertThat(employeeMapper.map(null, new Department())).isNull();
        }

        @Test
        void shouldMapValidEmployee() {
            EmployeeIn in = new EmployeeIn();
            in.setFirstName("John");
            in.setLastName("Doe");
            in.setEmail("john@example.com");
            in.setHireDate(Instant.now());

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
        void shouldMapToOut() {
            Department dept = new Department();
            dept.setId(1L);

            Employee employee = new Employee();
            employee.setId(2L);
            employee.setFirstName("John");
            employee.setLastName("Doe");
            employee.setEmail("john@example.com");
            employee.setHireDate(Instant.now());
            employee.setDepartment(dept);

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