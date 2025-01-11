package io.playground.web;

import io.playground.model.DepartmentIn;
import io.playground.model.DepartmentOut;
import io.playground.service.DepartmentService;
import jakarta.validation.Valid;
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}