package com.plant.backend.controller;

import com.plant.backend.dto.CareRecordDTO;
import com.plant.backend.entity.CareRecord;
import com.plant.backend.service.CareRecordService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/care/record")
@RequiredArgsConstructor
public class CareRecordController {

    private final CareRecordService careRecordService;
    private final JwtUtil jwtUtil;

    @PostMapping("/add")
    public Result<CareRecord> add(@RequestBody @Valid CareRecordDTO.AddRequest request, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        request.setUserId(userId);
        
        return Result.success(careRecordService.add(request));
    }

    @GetMapping("/statistic")
    public Result<Map<String, Object>> statistic(CareRecordDTO.StatQuery query, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        if (query.getUserId() == null) {
            query.setUserId(userId);
        }
        
        return Result.success(careRecordService.statistic(query, userId));
    }
}
