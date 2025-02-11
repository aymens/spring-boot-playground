package io.playground.web;

import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import io.playground.service.EmployeeService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeOut> create(@Valid @RequestBody EmployeeIn employee) {
        return ResponseEntity.ok(employeeService.create(employee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeOut> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<Page<EmployeeOut>> getByDepartmentId(@PathVariable Long departmentId,
                                                               @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(employeeService.getByDepartmentId(departmentId, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}