package io.playground.service;

import io.playground.domain.Domain;
import io.playground.mapper.Mapper;
import io.playground.model.Dto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The DomainService interface provides a generic contract for managing domain entities,
 * data transfer objects (DTOs), and associated repository and mapper operations.
 *
 * @param <Ety>  Represents the domain entity type that extends the {@link Domain} interface.
 * @param <ID>  Represents the type of the identifier of the domain entity.
 * @param <REPO>  Represents the type of the repository that extends JpaRepository for managing the domain entity.
 * @param <InDto>  Represents the input DTO type used for creating or updating domain entities.
 * @param <OuDto>  Represents the output DTO type used for retrieving information about domain entities.
 */
public interface DomainService<Ety extends Domain, ID, REPO extends JpaRepository<Ety, ID>, InDto extends Dto, OuDto extends Dto> {
    Class<Ety> getDomainClass();

    REPO getRepository();

    Mapper<Ety, InDto, OuDto> getMapper();

    boolean exists(ID id);

    Page<OuDto> getAll(Pageable pageable);

    void delete(ID id);

    OuDto create(InDto company);

    OuDto getById(ID id);
}
