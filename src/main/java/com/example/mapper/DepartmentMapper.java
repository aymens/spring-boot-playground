package com.example.mapper;

import com.example.domain.Company;
import com.example.domain.Department;
import com.example.model.DepartmentIn;
import com.example.model.DepartmentOut;
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
