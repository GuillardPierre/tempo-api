package com.tempo.application.model.worktimeSeries;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tempo.application.model.category.Category;
import com.tempo.application.model.recurrenceException.RecurrenceException;
import com.tempo.application.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Représente une série récurrente de créneaux.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorktimeSeries {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Category category;

    @NotNull(message = "StartDate cannot be null")
    // Format ISO-8601 (2025-04-26T13:30)
    private LocalDateTime startDate;
    private LocalDateTime endDate;    // nullable = récursif jusqu'à annulation

    // Format ISO-8601 (2025-04-26T13:30)
    private LocalDateTime startTime;  // Heure de début de chaque occurrence

    // Format ISO-8601 (2025-04-26T13:30)
    private LocalDateTime endTime;    // Heure de fin de chaque occurrence

    private String recurrence;       // RFC5545, ex. "FREQ=WEEKLY;BYDAY=MO,WE,FR"

    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecurrenceException> exceptions;

    @Transient
    public Long getDuration() {
        if (startTime == null || endTime == null) {
            return 0L;
        }

        try {
            Duration duration = Duration.between(startTime, endTime);
            return duration.toMinutes();
        } catch (Exception e) {
            return 0L;
        }
    }
}