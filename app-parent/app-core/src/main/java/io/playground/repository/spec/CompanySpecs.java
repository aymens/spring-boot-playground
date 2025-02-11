package io.playground.repository.spec;

import io.playground.domain.Company;
import io.playground.domain.Department_;
import io.playground.domain.Employee;
import io.playground.domain.Employee_;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CompanySpecs {
    //TODO withmindepts
    public static Specification<Company> withMinEmployees(Integer minEmployees) {
        return (root, query, criteriaBuilder) ->
                Optional.ofNullable(minEmployees)
                        .map(min -> {
                            var subquery = Objects.requireNonNull(query, "generate a better message here ").subquery(Long.class);
                            var employeeRoot = subquery.from(Employee.class);
                            subquery.select(criteriaBuilder.count(employeeRoot))
                                    .where(criteriaBuilder.equal(employeeRoot.get(Employee_.department).get(Department_.company), root));
                            return criteriaBuilder.ge(subquery, min);
                        }).orElse(null);
    }
}
