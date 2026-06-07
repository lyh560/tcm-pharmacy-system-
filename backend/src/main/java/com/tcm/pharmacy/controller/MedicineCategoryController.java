package com.tcm.pharmacy.controller;

import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.MedicineCategory;
import com.tcm.pharmacy.service.MedicineCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
public class MedicineCategoryController {

    @Autowired
    private MedicineCategoryService categoryService;

    @GetMapping("/list")
    public ApiResult<List<MedicineCategory>> list() {
        return ApiResult.success(categoryService.list());
    }

    @PostMapping
    public ApiResult<Boolean> save(@RequestBody MedicineCategory category) {
        return ApiResult.success(categoryService.save(category));
    }

    @PutMapping
    public ApiResult<Boolean> update(@RequestBody MedicineCategory category) {
        return ApiResult.success(categoryService.updateById(category));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable Long id) {
        return ApiResult.success(categoryService.removeById(id));
    }
}
