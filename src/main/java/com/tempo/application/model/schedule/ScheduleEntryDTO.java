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
    private String type;
    private Long seriesId; 
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime startDate; // Date de début pour les séries
    
    private Long duration;
    private String recurrence;
    private boolean isActive;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;

    // Factory method pour créer à partir d'un Worktime
    public static ScheduleEntryDTO fromWorktime(Worktime worktime) {
        if (worktime.isActive() && worktime.getEndTime() == null) {
            return null;
        }
        return ScheduleEntryDTO.builder()
                .id((long) worktime.getId())
                .type("SINGLE")
                .seriesId(worktime.getSeries() != null ? worktime.getSeries().getId() : null)
                .startTime(worktime.getStartTime())
                .endTime(worktime.getEndTime())
                .duration(worktime.getDuration())
                .recurrence(worktime.getRecurrencePattern())
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
                .startDate(workTimeSeries.getStartDate())
                .startTime(workTimeSeries.getStartTime())  
                .endTime(workTimeSeries.getEndTime())        
                .recurrence(workTimeSeries.getRecurrence())
                .duration(workTimeSeries.getDuration())
                .isActive(workTimeSeries.isActive())
                .categoryId((long) workTimeSeries.getCategory().getId())
                .categoryName(workTimeSeries.getCategory().getName())
                .build();
    }

    public static ScheduleEntryDTO fromActiveWorktime(Worktime worktime) {
        return ScheduleEntryDTO.builder()
                .id((long) worktime.getId())
                .type("CHRONO")
                .startTime(worktime.getStartTime())
                .isActive(worktime.isActive())
                .categoryId((long) worktime.getCategory().getId())
                .categoryName(worktime.getCategory().getName())
                .build();
    }
    
    // Alias pour assurer la compatibilité avec l'appel existant dans ScheduleService
    public static ScheduleEntryDTO fromWorktimeSeries(WorktimeSeries workTimeSeries) {
        return fromWorkTimeSeries(workTimeSeries);
    }
}