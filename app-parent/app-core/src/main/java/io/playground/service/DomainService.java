package io.playground.service;

import io.playground.domain.Domain;
import io.playground.mapper.Mapper;
import io.playground.model.Dto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Represents a generic service interface for operations on domain entities.
 * This interface is designed to work with domain-specific entities and their corresponding Data Transfer Objects (DTOs).
 * It provides methods for domain-specific transformations, repository access, and basic entity existence checks.
 *
 * @param <Ety>   The domain entity type that extends the {@link Domain} interface.
 * @param <ID>    The type of the identifier used by the domain entity.
 * @param <InDto> The input DTO type corresponding to the domain entity.
 * @param <OuDto> The output DTO type corresponding to the domain entity.
 */
public interface DomainService<Ety extends Domain, ID, InDto extends Dto, OuDto extends Dto> {
    Class<Ety> getDomainClass();

    JpaRepository<Ety, ID> getRepository();

    Mapper<Ety, InDto, OuDto> getMapper();

    boolean exists(ID id);

    List<OuDto> getAll();

    void delete(ID id);

    OuDto create(InDto company);

    OuDto getById(ID id);
}
