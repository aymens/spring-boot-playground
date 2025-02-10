package io.playground.service.impl;

import io.playground.domain.Domain;
import io.playground.exception.NotFoundException;
import io.playground.model.Dto;
import io.playground.service.DomainService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a base implementation of the {@link DomainService} interface.
 * This abstract class defines generic CRUD operations and other utility methods
 * for working with domain entities and their associated data transfer objects (DTOs).
 *
 * @param <Ety>   The type of the domain entity that extends the {@link Domain} interface.
 * @param <ID>    The type of the identifier of the domain entity.
 * @param <REPO>  The type of the repository that extends JpaRepository for managing the domain entity.
 * @param <InDto> The type of the input DTO used for creating/updating domain entities.
 * @param <OutDto> The type of the output DTO used for retrieving domain entity details.
 */
@Transactional
public abstract class BaseDomainServiceImpl<Ety extends Domain, ID, REPO extends JpaRepository<Ety, ID>,
        InDto extends Dto, OutDto extends Dto>
        implements DomainService<Ety, ID, REPO, InDto, OutDto> {

    @SuppressWarnings("unchecked")
    public Class<Ety> getDomainClass() {
        return (Class<Ety>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Transactional(readOnly = true)
    public boolean exists(ID id) {
        return getRepository().existsById(id);
    }

    public void delete(ID id) {
        if (!getRepository().existsById(id)) {
            throw NotFoundException.of(getDomainClass().getName(), id);
        }
        getRepository().deleteById(id);
    }

    @Transactional(readOnly = true)
    public OutDto getById(ID id) {
        return getRepository().findById(id)
                .map(getMapper()::map)
                .orElseThrow(() -> NotFoundException.of(getDomainClass().getName(), id));
    }

    @Transactional(readOnly = true)
    public List<OutDto> getAll() {
        return getRepository().findAll().stream()
                .map(getMapper()::map)
                .collect(Collectors.toList());
    }
}
