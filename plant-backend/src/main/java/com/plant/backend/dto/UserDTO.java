package com.plant.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class UserDTO {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^.{2,20}$", message = "用户名长度需在2-20位之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,16}$", message = "密码需6-16位，包含字母和数字")
        private String password;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class UpdateRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;

        @Email(message = "邮箱格式不正确")
        private String email;

        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        // 只有管理员可以修改角色
        private String role;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private Long userId;
        private String username;
        private String role;
        private Long expireTime;
    }

    @Data
    public static class Info {
        private Long userId;
        private String username;
        private String email;
        private String phone;
        private String role;
        private java.time.LocalDateTime createTime;
    }
}
