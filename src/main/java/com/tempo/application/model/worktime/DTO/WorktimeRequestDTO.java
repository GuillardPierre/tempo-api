package com.tempo.application.model.worktime.DTO;

import lombok.Data;

@Data
public class WorktimeRequestDTO {
    private Integer id;
    private String startTime;
    private String endTime;
    private int categoryId;
}
