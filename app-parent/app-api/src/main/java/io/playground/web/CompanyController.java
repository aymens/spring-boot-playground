package io.playground.web;

import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/companies")
@Transactional
@PreAuthorize("hasAnyRole('ROLE_app_user', 'ROLE_app_admin')")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyOut> create(@Valid @RequestBody CompanyIn company) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.create(company));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyOut> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CompanyOut>> getAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(companyService.getAll(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Find companies based on department and employee criteria")
    @GetMapping("/find")
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public ResponseEntity<Page<CompanyOut>> findCompanies(
            @Parameter(description = "Minimum number of departments")
            @RequestParam Optional<Integer> minDepartments,
            @Parameter(description = "Minimum number of employees")
            @RequestParam Optional<Integer> minEmployees,
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                minDepartments.map(deps ->
                        minEmployees.map(emps ->
                                companyService.findCompaniesWithMinDepartmentsAndEmployees(deps, emps, pageable)
                        ).orElseGet(() ->
                                companyService.findCompaniesWithMinDepartments(deps, pageable))
                ).orElseGet(() ->
                        minEmployees.map(emps ->
                                companyService.findCompaniesWithMinEmployees(emps, pageable)
                        ).orElseGet(() ->
                                companyService.getAll(pageable))
                )
        );
    }
}