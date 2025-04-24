package com.tempo.application.model.recurrenceException;

import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Périodes de pause dans une série récurrente (vacances, congés, etc.).
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurrenceException {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private WorktimeSeries series;

    private LocalDateTime pauseStart;
    private LocalDateTime pauseEnd;
}