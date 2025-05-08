package com.tempo.application.model.worktime;

import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tempo.application.model.category.Category;
import com.tempo.application.model.user.User;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Worktime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private WorktimeSeries series;   // null si créneau unique

    // Format ISO-8601 (2025-04-26T13:30)
    private LocalDateTime startTime;

    // Format ISO-8601 (2025-04-26T13:30)
    private LocalDateTime endTime;

    private boolean isActive;

    private String recurrencePattern;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Category category;

    @Builder.Default
    private boolean active = true;

    @Transient
    public Long getDuration() {
        if (startTime == null || endTime == null) {
            return null;
        }

        try {
            Duration duration = Duration.between(startTime, endTime);
            return duration.toMinutes();
        } catch (Exception e) {
            return null;
        }
    }

}
