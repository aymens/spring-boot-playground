package io.playground.web;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
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
    public ResponseEntity<List<DepartmentOut>> getByCompanyId(@PathVariable Long companyId) {
        return ResponseEntity.ok(departmentService.getByCompanyId(companyId));
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
    @GetMapping("/search")
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
            @ParameterObject // springdoc map query parameters to object
            Pageable pageable) {

        Page<DepartmentOut> page = departmentService.find(companyId, nameFilter, minEmployees, pageable);
        return ResponseEntity.ok(page);
    }
    //TODO test, master the pageable mvc handling, defaults, apply Page to all getAll* methods
    //
    // teach me mvc Pageable handling

}