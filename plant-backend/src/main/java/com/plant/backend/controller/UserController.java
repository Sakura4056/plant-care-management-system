package com.plant.backend.controller;

import com.plant.backend.dto.UserDTO;
import com.plant.backend.entity.User;
import com.plant.backend.service.UserService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<UserDTO.LoginResponse> register(@RequestBody @Valid UserDTO.RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<UserDTO.LoginResponse> login(@RequestBody @Valid UserDTO.LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PutMapping("/update")
    public Result<User> update(@RequestBody @Valid UserDTO.UpdateRequest request, HttpServletRequest httpRequest) {
        // 获取当前用户信息从token手动或从SecurityContext
        // 由于我们有一个Filter设置SecurityContext，让我们使用那个或解析器帮助。
        // 为简单起见，在这里重用util，因为需求描述中要求显式验证header，
        // 尽管Filter处理认证。我们需要ID来进行权限检查。

        String token = httpRequest.getHeader("Authorization").substring(7);
        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        // 安全/体验优化：如果是普通用户，强制修改自己的信息，忽略请求体中的 ID
        if (!"ADMIN".equals(role)) {
            request.setUserId(currentUserId);
        }

        return Result.success(userService.update(request, currentUserId, role));
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return Result.success();
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserDTO.Info>> list(
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "keyword", required = false) String keyword) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pageNum, pageSize);
        return Result.success(userService.listUsers(page, keyword));
    }
}
