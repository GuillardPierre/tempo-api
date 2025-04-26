package com.tempo.application.model.schedule;

import com.tempo.application.model.worktime.Worktime;
import com.tempo.application.model.worktimeSeries.WorktimeSeries;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntryDTO {
    private Long id;
    private String type; // "SINGLE" ou "RECURRING"
    private Long seriesId; // uniquement pour les entrées récurrentes
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // en minutes
    private String recurrencePattern;
    private boolean isActive;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;

    // Factory method pour créer à partir d'un Worktime
    public static ScheduleEntryDTO fromWorktime(Worktime worktime) {
        return ScheduleEntryDTO.builder()
                .id((long) worktime.getId())
                .type("SINGLE")
                .seriesId(worktime.getSeries() != null ? worktime.getSeries().getId() : null)
                .startTime(worktime.getStartTime())
                .endTime(worktime.getEndTime())
                .duration(worktime.getDuration())
                .recurrencePattern(worktime.getRecurrencePattern())
                .isActive(worktime.isActive())
                .categoryId((long) worktime.getCategory().getId())
                .categoryName(worktime.getCategory().getName())
                .build();
    }

    // Factory method pour créer à partir d'un WorktimeSeries
    public static ScheduleEntryDTO fromWorkTimeSeries(WorktimeSeries workTimeSeries) {
        return ScheduleEntryDTO.builder()
                .id(workTimeSeries.getId())
                .type("RECURRING")
                .startTime(workTimeSeries.getStartTime())    // Utilisation de startTime au lieu de startDate
                .endTime(workTimeSeries.getEndTime())        // Utilisation de endTime au lieu de endDate
                .recurrencePattern(workTimeSeries.getRecurrence())
                .duration(workTimeSeries.getDuration())
                .isActive(workTimeSeries.isActive())
                .categoryId((long) workTimeSeries.getCategory().getId())
                .categoryName(workTimeSeries.getCategory().getName())
                .build();
    }
    
    // Alias pour assurer la compatibilité avec l'appel existant dans ScheduleService
    public static ScheduleEntryDTO fromWorktimeSeries(WorktimeSeries workTimeSeries) {
        return fromWorkTimeSeries(workTimeSeries);
    }
}