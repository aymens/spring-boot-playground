package io.playground.web;

import io.playground.model.EmployeeIn;
import io.playground.model.EmployeeOut;
import io.playground.service.EmployeeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

@RestController
@RequestMapping("api/employees")
@Transactional
@PreAuthorize("hasAnyRole('ROLE_app_user', 'ROLE_app_admin')")
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

    @GetMapping("/find")
    public ResponseEntity<Page<EmployeeOut>> find(@RequestParam(required = false) Long departmentId,
                                                  @Parameter(
                                                          description = "Hire date (format: yyyy-MM-dd)",
                                                          example = "2024-03-21",
                                                          schema = @Schema(
                                                                  type = "string",
                                                                  pattern = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"
                                                          )
                                                  )
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                  // This allows simple date format input
                                                  @RequestParam(required = false) LocalDate hireDate,
                                                  @RequestParam(required = false) BigDecimal minSalary,
                                                  @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(
                employeeService.findByDepartmentIdHireDateMinSalary(
                        departmentId,
                        Optional.ofNullable(hireDate)
                                .map(d -> d.atStartOfDay(UTC).toInstant())
                                .orElse(null),
                        minSalary,
                        pageable
                ));
    }
}