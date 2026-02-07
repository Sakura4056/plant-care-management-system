package com.plant.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class PlantDTO {

    @Data
    public static class OfficialQuery {
        private String keyword; // 名称, 属, 种
        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }

    @Data
    public static class LocalAddRequest {
        private Long userId; // 实际上应从 Token 获取，但在 Controller 中填充
        @NotBlank
        private String name;
        private String genus;
        private String species;
        private String description;
    }

    @Data
    public static class AuditRequest {
        private Long reviewerId; // 通常从 Token 获取
        @NotBlank
        private String status; // APPROVED (通过), REJECTED (驳回)
        private String rejectReason;
    }
}
