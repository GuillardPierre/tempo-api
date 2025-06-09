package com.tempo.application.model.recurrenceException;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RecurrenceExceptionCreateDTO {
    private LocalDateTime pauseStart;
    private LocalDateTime pauseEnd;
} 