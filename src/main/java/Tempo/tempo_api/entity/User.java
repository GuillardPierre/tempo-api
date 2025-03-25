package Tempo.tempo_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*; 

@Entity
@Table(name = "users")
@Getter // Lombok annotation to create all getters
@Setter // Lombok annotation to create all setters
@NoArgsConstructor  // Lombok annotation to create no-args constructor
@AllArgsConstructor // Lombok annotation to create all-args constructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
}
