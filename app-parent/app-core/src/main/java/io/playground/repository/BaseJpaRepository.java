package io.playground.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * BaseJpaRepository is a generic repository interface that extends Spring Data JPA's JpaRepository
 * and JpaSpecificationExecutor interfaces. It provides basic CRUD operations as well as the capability
 * to execute JPA Criteria API-based specifications.
 * <br/><br/>
 * This interface is intended to serve as a base repository for all entity-specific repositories
 * in the application. It allows centralized functionality across repositories while leveraging
 * Spring Data's repository abstraction.
 * <br/><br/>
 * The `@NoRepositoryBean` annotation is used to indicate that this is not a repository bean itself
 * and should not be instantiated directly by the Spring container. Entity-specific repositories
 * can extend this interface to inherit its functionality.
 *
 * @param <ENTITY> the type of the entity to handle
 * @param <ID>     the type of the entity's identifier
 */
@NoRepositoryBean
public interface BaseJpaRepository<ENTITY, ID> extends JpaRepository<ENTITY, ID>,
        JpaSpecificationExecutor<ENTITY> {
}
