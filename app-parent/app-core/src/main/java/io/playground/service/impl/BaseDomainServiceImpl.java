package io.playground.service.impl;

import io.playground.domain.Domain;
import io.playground.exception.NotFoundException;
import io.playground.model.Dto;
import io.playground.service.DomainService;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract base implementation of the {@link DomainService} interface, providing generic operations
 * for managing and interacting with domain entities and their Data Transfer Objects (DTOs).
 *
 * @param <Ety>  The type of domain entity managed by the service, extending the {@link Domain} interface.
 * @param <ID> The type of the identifier used by the domain entity.
 * @param <InDto>  The input DTO type used for creating or updating the domain entity.
 * @param <OutDto>  The output DTO type used for retrieving domain entity information.
 */
@Transactional
public abstract class BaseDomainServiceImpl<Ety extends Domain, ID, InDto extends Dto, OutDto extends Dto>
        implements DomainService<Ety, ID, InDto, OutDto> {

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
