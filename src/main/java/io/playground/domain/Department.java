package io.playground.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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
    @ToString.Exclude // Prevent recursive toString()
    @EqualsAndHashCode.Exclude // Prevent recursion
    private Company company;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    @ToString.Exclude // Prevent recursive toString()
    @EqualsAndHashCode.Exclude // Prevent recursion
    private List<Employee> employees = new ArrayList<>();
}
