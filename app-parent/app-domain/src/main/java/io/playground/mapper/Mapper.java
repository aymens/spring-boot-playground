package io.playground.mapper;

import io.playground.domain.Domain;
import io.playground.model.Dto;

public interface Mapper <ETY extends Domain, IN extends Dto, OUT extends Dto> {
    ETY map(IN dto);
    OUT map(ETY domain);
}
