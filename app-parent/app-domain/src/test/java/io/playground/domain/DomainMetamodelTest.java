package io.playground.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/*
    - Metamodel classes are generated with static fields like `public static volatile SingularAttribute<Employee, String> name;`
    - These fields are meant to be initialized by the JPA provider at runtime
    - Without proper JPA configuration in test, Hibernate doesn't initialize these static fields

    Thus, the need for the SpringBootTest.
 */
@SpringBootTest
class DomainMetamodelTest {
    @Test
    @DisplayName("verify_entityMetamodel_allFieldsGenerated")
    void verify_entityMetamodel_allFieldsGenerated() {
        assertThat(Department_.name).isNotNull();
        assertThat(Department_.company).isNotNull();
        assertThat(Department_.id).isNotNull();
        assertThat(Department_.employees).isNotNull();

        assertThat(Company_.name).isNotNull();
        assertThat(Company_.taxId).isNotNull();
        assertThat(Company_.id).isNotNull();
        assertThat(Company_.departments).isNotNull();
        assertThat(Company_.createdAt).isNotNull();

        assertThat(Employee_.firstName).isNotNull();
        assertThat(Employee_.lastName).isNotNull();
        assertThat(Employee_.email).isNotNull();
        assertThat(Employee_.id).isNotNull();
        assertThat(Employee_.department).isNotNull();
        assertThat(Employee_.hireDate).isNotNull();
    }
}