package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.CareScheduleDTO;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.service.CareScheduleService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/care/schedule")
@RequiredArgsConstructor
public class CareScheduleController {

    private final CareScheduleService careScheduleService;
    private final JwtUtil jwtUtil;

    @PostMapping("/add")
    public Result<CareSchedule> add(@RequestBody @Valid CareScheduleDTO.AddRequest request,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        request.setUserId(userId);

        return Result.success(careScheduleService.add(request));
    }

    @PutMapping("/update/{scheduleId}")
    public Result<CareSchedule> update(@PathVariable("scheduleId") Long scheduleId,
            @RequestBody @Valid CareScheduleDTO.UpdateRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        request.setId(scheduleId);

        return Result.success(careScheduleService.updateSchedule(request, userId));
    }

    @DeleteMapping("/delete/{scheduleId}")
    public Result<Void> delete(@PathVariable("scheduleId") Long scheduleId, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        careScheduleService.deleteSchedule(scheduleId, userId);
        return Result.success();
    }

    @GetMapping("/query")
    public Result<Page<CareSchedule>> query(CareScheduleDTO.Query query, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        return Result.success(careScheduleService.query(query, userId, role));
    }
}
