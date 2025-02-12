package io.playground.repository.spec;

import io.playground.domain.*;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CompanySpecs {

    public static Specification<Company> withMinEmployees(Integer minEmployees) {
        return (root, query, criteriaBuilder) ->
                Optional.ofNullable(minEmployees)
                        .map(min -> {
                            val subquery = Objects.requireNonNull(query, "Query cannot be null").subquery(Long.class);
                            val employeeRoot = subquery.from(Employee.class);
                            subquery.select(criteriaBuilder.count(employeeRoot))
                                    .where(criteriaBuilder.equal(employeeRoot.get(Employee_.department).get(Department_.company), root));
                            return criteriaBuilder.ge(subquery, min);
                        }).orElse(null);
    }

    public static Specification<Company> withMinDepartments(Integer minDepartments) {
        return (root, query, criteriaBuilder) ->
                Optional.ofNullable(minDepartments)
                        .map(min -> {
                            val subquery = Objects.requireNonNull(query, "Query cannot be null").subquery(Long.class);
                            val departmentRoot = subquery.from(Department.class);
                            subquery.select(criteriaBuilder.count(departmentRoot))
                                    .where(criteriaBuilder.equal(departmentRoot.get(Department_.company), root));
                            return criteriaBuilder.ge(subquery, min);
                        }).orElse(null);
    }
}
