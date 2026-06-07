package com.tcm.pharmacy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.Medicine;
import com.tcm.pharmacy.service.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicine")
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @GetMapping("/page")
    public ApiResult<Page<Medicine>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        return ApiResult.success(medicineService.page(pageNum, pageSize, name, categoryId));
    }

    @GetMapping("/list")
    public ApiResult<List<Medicine>> list() {
        return ApiResult.success(medicineService.list());
    }

    @GetMapping("/{id}")
    public ApiResult<Medicine> getById(@PathVariable Long id) {
        return ApiResult.success(medicineService.getById(id));
    }

    @PostMapping
    public ApiResult<Boolean> save(@RequestBody Medicine medicine) {
        return ApiResult.success(medicineService.save(medicine));
    }

    @PutMapping
    public ApiResult<Boolean> update(@RequestBody Medicine medicine) {
        return ApiResult.success(medicineService.updateById(medicine));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable Long id) {
        return ApiResult.success(medicineService.removeById(id));
    }

    @GetMapping("/low-stock")
    public ApiResult<List<Medicine>> getLowStock() {
        return ApiResult.success(medicineService.getLowStockMedicines());
    }
}
