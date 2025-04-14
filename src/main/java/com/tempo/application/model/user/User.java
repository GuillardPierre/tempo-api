package com.tempo.application.model.user;

import java.util.List;

import com.tempo.application.model.category.Category;
import com.tempo.application.model.worktime.Worktime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    private String username;

    private String email;

    private String password;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Category> categories;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Worktime> worktimes;
}
