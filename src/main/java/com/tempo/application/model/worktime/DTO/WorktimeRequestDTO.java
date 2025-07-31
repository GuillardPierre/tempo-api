package com.tempo.application.model.worktime.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorktimeRequestDTO {
    private Integer id;
    private LocalDateTime startHour;
    private LocalDateTime endHour;
    private Category category;

    @Data
    public static class Category {
        private Integer id;
        private String name;
        
        @JsonProperty("title")
        public void setTitle(String title) {
            this.name = title;
        }
    }
}
