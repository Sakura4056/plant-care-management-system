package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.PlantDTO;
import com.plant.backend.entity.AuditOrder;
import com.plant.backend.entity.LocalPlant;
import com.plant.backend.entity.OfficialPlant;
import com.plant.backend.service.PlantService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plant")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;
    private final JwtUtil jwtUtil;

    @GetMapping("/official/query")
    public Result<Page<OfficialPlant>> queryOfficial(PlantDTO.OfficialQuery query) {
        return Result.success(plantService.queryOfficial(query));
    }

    @GetMapping("/local/list")
    public Result<java.util.List<LocalPlant>> queryLocal(
            @RequestParam(name = "keyword", required = false) String keyword,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(plantService.queryLocal(userId, keyword));
    }

    @PostMapping("/local/add")
    public Result<LocalPlant> addLocal(@RequestBody @Valid PlantDTO.LocalAddRequest request,
            HttpServletRequest httpRequest) {
        // Ensure userId in body matches token if user
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long tokenUserId = jwtUtil.getUserIdFromToken(token);

        // Override userId from token to be safe, or validate
        request.setUserId(tokenUserId);

        return Result.success(plantService.addLocal(request));
    }

    @DeleteMapping("/local/delete/{id}")
    public Result<Void> deleteLocal(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        plantService.deleteLocal(id, userId);
        return Result.success();
    }

    @PutMapping("/local/submit-audit/{localPlantId}")
    public Result<AuditOrder> submitAudit(@PathVariable("localPlantId") Long localPlantId,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        return Result.success(plantService.submitAudit(localPlantId, userId));
    }

    @PutMapping("/audit/{auditOrderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> audit(@PathVariable("auditOrderId") Long auditOrderId,
            @RequestBody @Valid PlantDTO.AuditRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long reviewerId = jwtUtil.getUserIdFromToken(token);

        request.setReviewerId(reviewerId);

        plantService.audit(auditOrderId, request);

        return Result.success();
    }

    @GetMapping("/audit/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<AuditOrder>> queryAuditOrders(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "status", required = false) String status) {
        Page<AuditOrder> page = new Page<>(pageNum, pageSize);
        return Result.success(plantService.queryAuditOrders(page, status));
    }
}
