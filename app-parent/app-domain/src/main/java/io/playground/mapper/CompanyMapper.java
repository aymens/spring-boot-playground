package io.playground.mapper;

import io.playground.domain.Company;
import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface CompanyMapper extends io.playground.mapper.Mapper<Company, CompanyIn, CompanyOut> {

    CompanyOut map(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Company map(CompanyIn in);
}