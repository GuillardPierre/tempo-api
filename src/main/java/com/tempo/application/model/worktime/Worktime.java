package com.tempo.application.model.worktime;



import com.tempo.application.model.category.Category;
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
public class Worktime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String startTime;
    private String endTime;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
