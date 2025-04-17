package com.tempo.application.model.worktime.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WorktimeRequestDTO {
    private Integer id;
    private String startTime;
    private String endTime;
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
