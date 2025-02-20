package io.playground.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "company")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
//TODO Map relationship
public class Company implements Domain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "tax_id", nullable = false, unique = true)
    private String taxId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    @ToString.Exclude // Prevent cycle
    @EqualsAndHashCode.Exclude // Prevent cycle
    @JsonManagedReference
    private List<Department> departments;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
