package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plant.backend.dto.UserDTO;
import com.plant.backend.entity.User;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.mysql.UserMapper;

import com.plant.backend.service.UserService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = { @Lazy })
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO.LoginResponse register(UserDTO.RegisterRequest request) {
        // 检查唯一性
        if (count(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("USER"); // 默认角色

        save(user);

        return generateLoginResponse(user);
    }

    @Override
    public UserDTO.LoginResponse login(UserDTO.LoginRequest request) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名或密码错误");
        }

        return generateLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User update(UserDTO.UpdateRequest request, Long currentUserId, String currentRole) {
        // 权限检查：管理员可以更新任何人，用户只能更新自己
        if (!"ADMIN".equals(currentRole) && !currentUserId.equals(request.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        User user = getById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        if (request.getEmail() != null)
            user.setEmail(request.getEmail());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());

        // 只有管理员可以修改角色
        if ("ADMIN".equals(currentRole) && request.getRole() != null) {
            user.setRole(request.getRole());
        }

        updateById(user);

        // 返回前隐藏密码
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        // MySQL 逻辑删除 (MP @TableLogic 处理)
        removeById(userId);

        // 发布删除事件，解耦下游清理逻辑
        eventPublisher.publishEvent(new com.plant.backend.event.UserDeletedEvent(this, userId));
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserDTO.Info> listUsers(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> result = page(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserDTO.Info> dtoPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        dtoPage.setCurrent(result.getCurrent());
        dtoPage.setSize(result.getSize());
        dtoPage.setTotal(result.getTotal());

        java.util.List<UserDTO.Info> dtos = result.getRecords().stream().map(u -> {
            UserDTO.Info info = new UserDTO.Info();
            info.setUserId(u.getUserId());
            info.setUsername(u.getUsername());
            info.setEmail(u.getEmail());
            info.setPhone(u.getPhone());
            info.setRole(u.getRole());
            info.setCreateTime(u.getCreateTime());
            return info;
        }).collect(java.util.stream.Collectors.toList());

        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    private UserDTO.LoginResponse generateLoginResponse(User user) {
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        UserDTO.LoginResponse response = new UserDTO.LoginResponse();
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        // Simple expire time calc, should match JWT config
        response.setExpireTime(System.currentTimeMillis() + 86400000);

        return response;
    }
}
