package com.tempo.application.model.category;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tempo.application.model.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @JsonBackReference(value = "user-category")
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private User user;
}
