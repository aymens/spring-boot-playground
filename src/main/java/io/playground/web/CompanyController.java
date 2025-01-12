package io.playground.web;

import io.playground.model.CompanyIn;
import io.playground.model.CompanyOut;
import io.playground.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
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

    @Validated

    @GetMapping("/{id}")
    public ResponseEntity<CompanyOut> getById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CompanyOut>> getAll() {
        return ResponseEntity.ok(companyService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}