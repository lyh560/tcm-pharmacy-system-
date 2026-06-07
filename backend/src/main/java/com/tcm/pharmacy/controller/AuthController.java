package com.tcm.pharmacy.controller;

import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.dto.LoginRequest;
import com.tcm.pharmacy.dto.LoginResponse;
import com.tcm.pharmacy.entity.User;
import com.tcm.pharmacy.service.UserService;
import com.tcm.pharmacy.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return ApiResult.error(401, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResult.error(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponse response = new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());

        return ApiResult.success(response);
    }

    @GetMapping("/me")
    public ApiResult<LoginResponse> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ApiResult.error(401, "未登录");
        }

        String token = authorization.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ApiResult.error(401, "Token无效或已过期");
        }

        String username = jwtUtil.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            return ApiResult.error(401, "用户不存在");
        }

        LoginResponse response = new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());
        return ApiResult.success(response);
    }
}
