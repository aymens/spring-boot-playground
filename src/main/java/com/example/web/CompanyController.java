package com.example.web;

import com.example.model.CompanyIn;
import com.example.model.CompanyOut;
import com.example.service.CompanyService;
import jakarta.validation.Valid;
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
        return ResponseEntity.ok(companyService.create(company));
    }

    @Validated

    @GetMapping("/{id}")
    public ResponseEntity<CompanyOut> getById(@PathVariable Long id) {
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