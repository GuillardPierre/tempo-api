package com.tempo.application.model.worktime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
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
    
    @Enumerated(EnumType.STRING)
    private WorktimeType type;

    private boolean isActive;

    private String recurrencePattern;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @JsonBackReference(value = "user-worktime")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Transient
    public Long getDuration() {
        if (startTime == null || endTime == null) {
            return 0L;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            
            Duration duration = Duration.between(start, end);
            return duration.toMinutes();
        } catch (Exception e) {
            return 0L;
        }
    }


    public enum WorktimeType {
        PLANNED,    // Temps planifié
        TRACKED,    // Temps chronométré
        RECURRENT   // Temps récurrent
    }
}
