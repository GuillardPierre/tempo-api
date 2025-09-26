package com.tempo.application.model.recurrenceException;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO pour créer une exception spécifique à une WorktimeSeries pour une journée
 * donnée
 */
@Data
public class WorktimeSeriesExceptionCreateDTO {

    @NotNull(message = "Series ID cannot be null")
    private Long seriesId;

    @NotNull(message = "Date cannot be null")
    private LocalDate date;
}
