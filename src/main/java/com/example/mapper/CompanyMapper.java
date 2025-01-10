package com.example.mapper;

import com.example.domain.Company;
import com.example.model.CompanyIn;
import com.example.model.CompanyOut;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface CompanyMapper {

    CompanyOut map(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Company map(CompanyIn in);
}