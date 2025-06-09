package com.tempo.application.model.worktimeSeries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorktimeSeriesResponseDTO {
    private Long id;
    private String type; // "RECURRING"
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int categoryId;
    private String categoryName;
    private String recurrence;
    private Long duration; // Durée en minutes

    public static WorktimeSeriesResponseDTO fromEntity(WorktimeSeries series) {
        return WorktimeSeriesResponseDTO.builder()
                .id(series.getId())
                .type("RECURRING")
                .startDate(series.getStartDate())
                .endDate(series.getEndDate())
                .startTime(series.getStartTime())
                .endTime(series.getEndTime())
                .categoryId(series.getCategory().getId())
                .categoryName(series.getCategory().getName())
                .recurrence(series.getRecurrence())
                .duration(series.getDuration())
                .build();
    }
}
