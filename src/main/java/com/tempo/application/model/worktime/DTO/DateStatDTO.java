package com.tempo.application.model.worktime.DTO;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateStatDTO {
    private LocalDate date;
    private int duration;
} 