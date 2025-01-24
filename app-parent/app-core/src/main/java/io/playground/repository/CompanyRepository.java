package io.playground.repository;

import io.playground.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByTaxId(String taxId);
}
