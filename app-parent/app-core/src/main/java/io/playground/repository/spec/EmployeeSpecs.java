package io.playground.repository.spec;

import io.playground.domain.Department_;
import io.playground.domain.Employee;
import io.playground.domain.Employee_;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Utility class providing static methods to create JPA Specifications for the Employee entity.
 * This class is declared final and has a private constructor to prevent instantiation.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmployeeSpecs {
    /**
     * Creates a specification to filter {@link Employee} entities based on their hire date.
     * If the provided hire date is not null, the specification filters employees hired on or
     * after the specified date. If the hire date is null, no filtering is applied.
     *
     * @param since the date from which employees are to be filtered; can be null.
     * @return a {@link Specification} for filtering employees by hire date, or null
     *         if the specified date is null.
     */
    public static Specification<Employee> hiredSince(Instant since) {
        return (root, _, cb) -> Optional.ofNullable(since)
                .map(date -> cb.greaterThanOrEqualTo(root.get(Employee_.hireDate), date))
                .orElse(null);
    }

    /**
     * Creates a specification to filter {@link Employee} entities by their associated department ID.
     * If the provided departmentId is null, the specification will not apply any filtering criteria.
     *
     * @param departmentId the ID of the department to filter employees by. If null, no filtering will be applied.
     * @return a {@link Specification} to filter employees by department ID, or null if the departmentId is null.
     */
    public static Specification<Employee> inDepartment(Long departmentId) {
        return (root, query, cb) ->
                Optional.ofNullable(departmentId)
                        .map(id -> cb.equal(root.get(Employee_.department).get(Department_.ID), id))
                        .orElse(null);

    }

    /**
     * Creates a specification to filter {@link Employee} entities based on a minimum salary value.
     * If the provided minimum salary is null, no filtering will be applied.
     *
     * @param minSalary the minimum salary to filter employees by. May be null, in which case no filter is applied.
     * @return a {@link Specification} for filtering employees with a salary greater than or equal to the specified value,
     *         or null if the provided minimum salary is null.
     */
    public static Specification<Employee> withMinSalary(BigDecimal minSalary) {
        return (root, _, cb) -> Optional.ofNullable(minSalary)
                .map(salary -> cb.greaterThanOrEqualTo(
                        root.get(Employee_.salary),
                        salary
                ))
                .orElse(null);
    }
}