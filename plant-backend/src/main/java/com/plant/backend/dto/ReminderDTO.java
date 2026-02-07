package com.plant.backend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

public class ReminderDTO {

    @Data
    public static class ConfigUpdateRequest {
        private String email;
        private String phone;
        private Integer popupEnabled;
        private Integer bellEnabled;
        private String sceneConfig;
    }
    
    @Data
    public static class UnreadResponse {
        private Long totalUnread;
        // Map<Scene, List<Reminder>>
        private Map<String, List<Object>> details;
    }
}
