package com.tcm.pharmacy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.User;
import com.tcm.pharmacy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/page")
    public ApiResult<Page<User>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return ApiResult.success(userService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/list")
    public ApiResult<List<User>> list() {
        return ApiResult.success(userService.list());
    }

    @PostMapping
    public ApiResult<Boolean> save(@RequestBody User user) {
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ApiResult.success(userService.save(user));
    }

    @PutMapping
    public ApiResult<Boolean> update(@RequestBody User user) {
        // 不更新密码
        user.setPassword(null);
        return ApiResult.success(userService.updateById(user));
    }

    @PostMapping("/{id}/reset-password")
    public ApiResult<Boolean> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResult.error(404, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        return ApiResult.success(userService.updateById(user));
    }

    @PostMapping("/{id}/toggle-status")
    public ApiResult<Boolean> toggleStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResult.error(404, "用户不存在");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        return ApiResult.success(userService.updateById(user));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable Long id) {
        return ApiResult.success(userService.removeById(id));
    }
}
