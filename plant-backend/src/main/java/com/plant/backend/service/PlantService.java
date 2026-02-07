package com.plant.backend.service;

public interface PlantService {
        // Official Plant Query
        // Official Plant Query
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.plant.backend.entity.OfficialPlant> queryOfficial(
                        com.plant.backend.dto.PlantDTO.OfficialQuery query);

        // Local Plant List
        java.util.List<com.plant.backend.entity.LocalPlant> queryLocal(Long userId, String keyword);

        // Local Plant Add
        com.plant.backend.entity.LocalPlant addLocal(com.plant.backend.dto.PlantDTO.LocalAddRequest request);

        // Local Plant Delete (Cascade)
        void deleteLocal(Long localPlantId, Long userId);

        // Submit Audit
        com.plant.backend.entity.AuditOrder submitAudit(Long localPlantId, Long userId);

        // Audit
        // Audit
        void audit(Long auditOrderId, com.plant.backend.dto.PlantDTO.AuditRequest request);

        // Audit Query
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.plant.backend.entity.AuditOrder> queryAuditOrders(
                        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.plant.backend.entity.AuditOrder> page,
                        String status);

}
