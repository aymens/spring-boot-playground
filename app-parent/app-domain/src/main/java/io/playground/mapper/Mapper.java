package io.playground.mapper;

import io.playground.domain.Domain;
import io.playground.model.Dto;

public interface Mapper <S extends Domain, T extends Dto, U extends Dto> {
    S map(T dto);
    U map(S domain);
}
