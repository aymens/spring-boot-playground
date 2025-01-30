package io.playground.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "department")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude // Prevent cycle
    @EqualsAndHashCode.Exclude // Prevent cycle
    @JsonBackReference
    private Company company;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @ToString.Exclude // Prevent cycle
    @EqualsAndHashCode.Exclude // Prevent cycle
    @JsonManagedReference
    private List<Employee> employees;
}
