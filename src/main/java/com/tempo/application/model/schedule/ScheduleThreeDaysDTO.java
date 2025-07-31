package com.tempo.application.model.schedule;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleThreeDaysDTO {
    private List<ScheduleEntryDTO> yesterday;
    private List<ScheduleEntryDTO> today;
    private List<ScheduleEntryDTO> tomorrow;
} 