package com.tempo.application.model.recurrenceException;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecurrenceExceptionDTO {
    private Long id;
    private LocalDateTime pauseStart;
    private LocalDateTime pauseEnd;
    private List<Long> seriesIds; // Uniquement les IDs des séries
    private ExceptionType exceptionType;
    private Long targetSeriesId;

    public static RecurrenceExceptionDTO fromEntity(RecurrenceException entity) {
        RecurrenceExceptionDTO dto = new RecurrenceExceptionDTO();
        dto.setId(entity.getId());
        dto.setPauseStart(entity.getPauseStart());
        dto.setPauseEnd(entity.getPauseEnd());
        dto.setExceptionType(entity.getExceptionType());
        dto.setTargetSeriesId(entity.getTargetSeriesId());

        // Gérer le cas où series est null
        if (entity.getSeries() != null) {
            dto.setSeriesIds(entity.getSeries().stream()
                    .filter(series -> series != null)
                    .map(series -> series.getId())
                    .toList());
        } else {
            dto.setSeriesIds(List.of());
        }
        return dto;
    }
}