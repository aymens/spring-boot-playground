package io.playground.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * A base repository interface that extends functionality provided by {@link JpaRepository}
 * and {@link JpaSpecificationExecutor}. This interface serves as a foundation for creating
 * JPA-based repository interfaces in the application, enabling CRUD operations, dynamic
 * query execution, and specification pattern support.
 *
 * The {@code @NoRepositoryBean} annotation ensures that Spring Data JPA does not create
 * an instance of this repository interface directly. Instead, it is meant to be extended
 * by concrete repository interfaces.
 *
 * @param <ENTITY> the type of the entity to handle
 * @param <ID>     the type of the entity's identifier
 */
@NoRepositoryBean
public interface BaseJpaRepository<ENTITY, ID>
        extends JpaRepository<ENTITY, ID>, JpaSpecificationExecutor<ENTITY> {
//    Page<ENTITY> findAll(Specification<ENTITY> spec, Pageable pageable);
}
