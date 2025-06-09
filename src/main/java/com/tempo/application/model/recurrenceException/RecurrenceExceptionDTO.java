package com.tempo.application.model.recurrenceException;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecurrenceExceptionDTO {
    private Long id;
    private LocalDateTime pauseStart;
    private LocalDateTime pauseEnd;
    private List<Long> seriesIds;  // Uniquement les IDs des séries

    public static RecurrenceExceptionDTO fromEntity(RecurrenceException entity) {
        RecurrenceExceptionDTO dto = new RecurrenceExceptionDTO();
        dto.setId(entity.getId());
        dto.setPauseStart(entity.getPauseStart());
        dto.setPauseEnd(entity.getPauseEnd());
        dto.setSeriesIds(entity.getSeries().stream()
            .map(series -> series.getId())
            .toList());
        return dto;
    }
} 