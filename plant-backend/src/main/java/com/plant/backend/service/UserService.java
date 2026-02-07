package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.UserDTO;
import com.plant.backend.entity.User;

public interface UserService extends IService<User> {
    UserDTO.LoginResponse register(UserDTO.RegisterRequest request);

    UserDTO.LoginResponse login(UserDTO.LoginRequest request);

    User update(UserDTO.UpdateRequest request, Long currentUserId, String currentRole);

    void deleteUser(Long userId);

    com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserDTO.Info> listUsers(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page, String keyword);
}
