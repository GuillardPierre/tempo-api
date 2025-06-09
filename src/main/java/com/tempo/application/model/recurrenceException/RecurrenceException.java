package com.tempo.application.model.recurrenceException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Périodes de pause dans une série récurrente (vacances, congés, etc.).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "recurrence_exception")
public class RecurrenceException {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
        name = "recurrence_exception_series",
        joinColumns = @JoinColumn(name = "exception_id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "series_id", nullable = false)
    )
    @JsonIgnoreProperties("exceptions")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    @Builder.Default
    private List<WorktimeSeries> series = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime pauseStart;
    
    @Column(nullable = false)
    private LocalDateTime pauseEnd;
}