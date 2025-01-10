package com.example.mapper;

import com.example.domain.Department;
import com.example.domain.Employee;
import com.example.model.EmployeeIn;
import com.example.model.EmployeeOut;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING/*, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS*/)
public interface EmployeeMapper {//TODO test

    @Mapping(target = "departmentId", source = "department.id")
    EmployeeOut map(Employee employee);

    @Mapping(target = "department", source = "department")
    @Mapping(target = "id", ignore = true)
    Employee map(@MappingTarget Employee employee, EmployeeIn employeeIn, Department department);

    default Employee map(EmployeeIn employee, Department department) {
        if (employee == null || department == null) {
            return null;
        }
        return map(new Employee(), employee, department);
    }
}