package io.playground.mapper;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface DepartmentMapper extends io.playground.mapper.Mapper<Department, DepartmentIn, DepartmentOut> {

    @Mapping(target = "companyId", source = "company.id")
    DepartmentOut map(Department department);

    @Mapping(target = "company", source = "company")
    @Mapping(target = "name", source = "department.name")
    @Mapping(target = "id", ignore = true) // Explicitly ignore ID
    @Mapping(target = "employees", ignore = true)
    Department map(@MappingTarget Department targetDepartment, DepartmentIn department, Company company);

    default Department map(DepartmentIn in, Company company) {
        if (in == null || company == null) {
            return null;
        }
        return map(new Department(), in, company);
    }
}
