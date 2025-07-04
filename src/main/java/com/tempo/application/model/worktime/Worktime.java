package com.tempo.application.model.worktime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tempo.application.model.category.Category;
import com.tempo.application.model.recurrenceException.RecurrenceException;
import com.tempo.application.model.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Worktime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Format ISO-8601 (2025-04-26T13:30)
    private LocalDateTime startTime;

    // Format ISO-8601 (2025-04-26T13:30)
    private LocalDateTime endTime;

    @ManyToOne(optional = false)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @JsonIgnoreProperties({"worktimes", "password"})
    private User user;

    @ManyToOne(optional = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @JsonIgnoreProperties("worktimes")
    private Category category;

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
