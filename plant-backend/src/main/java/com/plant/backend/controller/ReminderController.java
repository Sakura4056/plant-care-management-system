package com.plant.backend.controller;

import com.plant.backend.dto.ReminderDTO;
import com.plant.backend.entity.ReminderConfig;
import com.plant.backend.service.ReminderService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminder")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;
    private final JwtUtil jwtUtil;

    @PutMapping("/config/update")
    public Result<ReminderConfig> updateConfig(@RequestBody ReminderDTO.ConfigUpdateRequest request,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        return Result.success(reminderService.updateConfig(request, userId));
    }

    @GetMapping("/unread/{userId}")
    public Result<ReminderDTO.UnreadResponse> getUnread(@PathVariable("userId") Long userId,
            HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long tokenUserId = jwtUtil.getUserIdFromToken(token);

        // 验证: 用户只能查看自己的提醒
        if (!userId.equals(tokenUserId)) {
            // 检查是否管理员? 或严格模式?
            // "普通用户仅能查自身"
            // 让我们严格检查，或允许管理员。
            // 目前最简单的是检查匹配。
            String role = jwtUtil.getRoleFromToken(token);
            if (!"ADMIN".equals(role)) {
                // 返回 Forbidden 还是只使用 Token ID?
                // 需求: "Path parameter userId". 安全的方法是验证。
                // 如果不匹配且不是管理员，我们将返回错误。
                // 目前，为了安全起见，如果通过了 ID 无关紧要，但规范说路径参数。
                return Result.error(com.plant.backend.util.ResultCode.FORBIDDEN);
            }
        }

        return Result.success(reminderService.getUnread(userId));
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);

        reminderService.markAsRead(id, userId);
        return Result.success();
    }
}
