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
    private LocalDateTime startHour;
    private LocalDateTime endHour;
    private LocalDateTime startDate;
    private LocalDateTime endDate; 
    private Long duration;
    private String recurrence;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private Boolean ignoreExceptions;


    // Factory method pour créer à partir d'un Worktime
    public static ScheduleEntryDTO fromWorktime(Worktime worktime) {
        // Vérification isActive() supprimée - fonctionnalité retirée
        return ScheduleEntryDTO.builder()
                .id((long) worktime.getId())
                .type("SINGLE")
                .startHour(worktime.getStartHour())
                .endHour(worktime.getEndHour())
                .duration(worktime.getDuration())
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
                .endDate(workTimeSeries.getEndDate())
                .startHour(workTimeSeries.getStartHour())  
                .endHour(workTimeSeries.getEndHour())        
                .recurrence(workTimeSeries.getRecurrence())
                .duration(workTimeSeries.getDuration())
                .categoryId((long) workTimeSeries.getCategory().getId())
                .categoryName(workTimeSeries.getCategory().getName())
                .ignoreExceptions(workTimeSeries.getIgnoreExceptions())
                .build();
    }

    
    // Alias pour assurer la compatibilité avec l'appel existant dans ScheduleService
    public static ScheduleEntryDTO fromWorktimeSeries(WorktimeSeries workTimeSeries) {
        return fromWorkTimeSeries(workTimeSeries);
    }
}