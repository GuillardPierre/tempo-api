package com.tempo.application.model.schedule;

import java.time.LocalDateTime;

import com.tempo.application.model.worktime.Worktime;
import com.tempo.application.model.worktimeSeries.WorktimeSeries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDateEntryDTO {
    private Long id;
    private String type; // "SINGLE" ou "RECURRING"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String recurrence;
    private LocalDateTime startDate;
    private LocalDateTime endDate;


    public static ScheduleDateEntryDTO fromWorktime(Worktime worktime) {
        return ScheduleDateEntryDTO.builder()
                .id((long) worktime.getId())
                .type("SINGLE")
                .startTime(worktime.getStartTime())
                .endTime(worktime.getEndTime())
                .build();
    }

    public static ScheduleDateEntryDTO fromWorktimeSeries(WorktimeSeries workTimeSeries) {
        return ScheduleDateEntryDTO.builder()
                .id(workTimeSeries.getId())
                .type("RECURRING")
                .startTime(workTimeSeries.getStartTime())
                .endTime(workTimeSeries.getEndTime())
                .recurrence(workTimeSeries.getRecurrence())
                .startDate(workTimeSeries.getStartDate())
                .endDate(workTimeSeries.getEndDate())
                .build();
    }
}
