package com.tcm.pharmacy.controller;

import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.InventoryBatch;
import com.tcm.pharmacy.service.InventoryBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @GetMapping("/batches")
    public ApiResult<List<InventoryBatch>> getBatches(@RequestParam Long medicineId) {
        return ApiResult.success(inventoryBatchService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InventoryBatch>()
                        .eq(InventoryBatch::getMedicineId, medicineId)
                        .eq(InventoryBatch::getStatus, 1)
                        .gt(InventoryBatch::getQuantity, 0)));
    }

    @GetMapping("/expiring")
    public ApiResult<List<InventoryBatch>> getExpiringBatches(
            @RequestParam(defaultValue = "30") int days) {
        return ApiResult.success(inventoryBatchService.getExpiringBatches(days));
    }
}
