package io.playground.repository.spec;

import io.playground.domain.Company_;
import io.playground.domain.Department;
import io.playground.domain.Department_;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.function.Predicate;

import static java.text.MessageFormat.format;

/**
 * Utility class providing static methods to create JPA Specifications for the Department entity.
 * This class is declared final and has a private constructor to prevent instantiation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DepartmentSpecs {

    /**
     * Creates a specification to filter {@link Department} entities based on the
     * associated company's ID.
     *
     * @param companyId the ID of the company to filter by. If null, no filtering
     *                  will be applied for the department's company.
     * @return a {@link Specification} for filtering departments by company ID, or null
     * if the provided companyId is null.
     */
    public static Specification<Department> hasCompanyId(Long companyId) {
        return (root, _, cb) ->
                Optional.ofNullable(companyId)
                        .map(_ ->
                                cb.equal(
                                        root.get(Department_.company).get(Company_.id),
                                        companyId))
                        .orElse(null);

    }

    /**
     * Creates a specification for filtering departments whose names contain the given substring,
     * ignoring case. If the provided name is null or blank, the specification returns all records.
     *
     * @param name the substring to search for in department names; can be null or blank.
     * @return a specification to filter departments by name, or null if the name is null or blank.
     */
    public static Specification<Department> nameContains(String name) {
        return (root, _, cb) ->
                Optional.ofNullable(name)
                        .filter(Predicate.not(String::isBlank))
                        .map(_ ->
                                cb.like(
                                        cb.upper(root.get(Department_.name)),
                                        format("%{0}%", name.toUpperCase())
                                ))
                        .orElse(null);

    }

    /**
     * Creates a specification to filter departments based on the minimum number of employees.
     * The specification checks if the size of the employees list in a department is greater than or
     * equal to the specified count.
     *
     * @param count the minimum number of employees required in a department; if null, no filtering is applied.
     * @return a specification to filter departments based on the minimum number of employees,
     * or null if the count is null.
     */
    public static Specification<Department> hasMinEmployees(Integer count) {
        return (root, _, cb) -> Optional.ofNullable(count)
                .map(_ ->
                        cb.ge(
                                cb.size(root.get(Department_.employees)),
                                count))
                .orElse(null);
    }
}