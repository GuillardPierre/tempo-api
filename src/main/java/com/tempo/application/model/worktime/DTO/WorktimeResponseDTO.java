package com.tempo.application.model.worktime.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.tempo.application.model.worktime.Worktime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorktimeResponseDTO {
    private Integer id;
    private String type; // "SINGLE"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isActive;
    private Integer categoryId;
    private String categoryName;
    private Long duration; // Durée en minutes

    public static WorktimeResponseDTO fromEntity(Worktime worktime) {
        return WorktimeResponseDTO.builder()
                .id(worktime.getId())
                .type("SINGLE")
                .startTime(worktime.getStartTime())
                .endTime(worktime.getEndTime())
                .isActive(worktime.isActive())
                .categoryId(worktime.getCategory().getId())
                .categoryName(worktime.getCategory().getName())
                .duration(worktime.getDuration())
                .build();
    }
}
