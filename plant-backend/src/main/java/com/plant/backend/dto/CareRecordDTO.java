package com.plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class CareRecordDTO {

    @Data
    public static class AddRequest {
        @NotNull
        private Long userId;
        @NotNull
        private Long plantId;
        @NotNull
        private String plantSource;
        private Long scheduleId;
        
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime recordTime;
        
        @NotNull
        private String operations; // 来自前端的 JSON 字符串
        
        private String remarks;
    }

    @Data
    public static class StatQuery {
        private Long userId;
        private Long plantId;
        private String plantSource;
        private Integer days; // 30 or 90
    }
    
    @Data
    public static class StatResponse {
        // 基于 "趋势图数据" 定义结构
        // 例如 dates: [], water: [], fertilizer: []
        // 或结构化数据
        private Object chartData; 
    }
}
