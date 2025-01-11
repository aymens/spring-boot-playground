package io.playground.web;

import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import io.playground.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<EmployeeOut>> getByDepartmentId(@PathVariable Long departmentId) {
        return ResponseEntity.ok(employeeService.getByDepartmentId(departmentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}