package io.playground.mapper;

import io.playground.domain.Company;
import io.playground.domain.Department;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING/*, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS*/)
public interface DepartmentMapper {//TODO test

    @Mapping(target = "companyId", source = "company.id")
    DepartmentOut map(Department department);

//    @BeanMapping(
//            nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
//            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
//    )
//    @Mapping(target = "company", source = "company")
//    @Mapping(target = "name", source = "department.name")
//    @Mapping(target = "id", ignore = true)
//    // Explicitly ignore ID
//    Department map(DepartmentIn department, Company company);

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
