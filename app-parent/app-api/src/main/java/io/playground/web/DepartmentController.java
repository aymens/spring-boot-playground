package io.playground.web;

import io.playground.configuration.security.permissions.CanEditCompanies;
import io.playground.domain.Department_;
import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/departments")
@Transactional
@CanEditCompanies
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentOut> create(@Valid @RequestBody DepartmentIn department) {
        return ResponseEntity.ok(departmentService.create(department));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentOut> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<DepartmentOut>> getByCompanyId(
            @PathVariable Long companyId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(departmentService.getByCompanyId(companyId, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(required = false) Long transferToId) {
        departmentService.delete(id, transferToId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search departments with filtering and pagination")
    @Parameter(name = "companyId", description = "Filter by company ID")
    @Parameter(name = "nameFilter", description = "Filter departments by name (case-insensitive)")
    @Parameter(name = "minEmployees", description = "Filter departments by minimum number of employees")
    @GetMapping("/find")
    public ResponseEntity<Page<DepartmentOut>> find(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String nameFilter,
            @RequestParam(required = false) Integer minEmployees,
            @PageableDefault(
                    size = 20
            )
            @SortDefault.SortDefaults({
                    @SortDefault(sort = Department_.NAME, direction = Sort.Direction.ASC),
                    @SortDefault(sort = Department_.ID, direction = Sort.Direction.DESC)
            })
            @ParameterObject // tells springdoc to map request params to object
            Pageable pageable) {

        Page<DepartmentOut> page = departmentService.find(companyId, nameFilter, minEmployees, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Find departments by employee count range")
    @GetMapping("/find/by-employee-count")
    public ResponseEntity<Page<DepartmentOut>> findByEmployeeCountRange(
            @RequestParam Long companyId,
            @RequestParam int minEmployees,
            @RequestParam int maxEmployees,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(departmentService.findByCompanyIdAndEmployeeCountBetween(
                companyId, minEmployees, maxEmployees, pageable));
    }

    @Operation(summary = "Find department with most recent hire in a company")
    @GetMapping("/find/most-recent-hire")
    public ResponseEntity<DepartmentOut> findDepartmentWithMostRecentHire(
            @RequestParam Long companyId) {
        return departmentService.findDepartmentWithMostRecentHire(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}