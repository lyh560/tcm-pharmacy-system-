package com.tcm.pharmacy.controller;

import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.Supplier;
import com.tcm.pharmacy.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/list")
    public ApiResult<List<Supplier>> list() {
        return ApiResult.success(supplierService.list());
    }

    @GetMapping("/{id}")
    public ApiResult<Supplier> getById(@PathVariable Long id) {
        return ApiResult.success(supplierService.getById(id));
    }

    @PostMapping
    public ApiResult<Boolean> save(@RequestBody Supplier supplier) {
        return ApiResult.success(supplierService.save(supplier));
    }

    @PutMapping
    public ApiResult<Boolean> update(@RequestBody Supplier supplier) {
        return ApiResult.success(supplierService.updateById(supplier));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable Long id) {
        return ApiResult.success(supplierService.removeById(id));
    }
}
