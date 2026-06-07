package com.tcm.pharmacy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.pharmacy.entity.InventoryBatch;
import com.tcm.pharmacy.mapper.InventoryBatchMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryBatchService extends ServiceImpl<InventoryBatchMapper, InventoryBatch> {

    public List<InventoryBatch> getExpiringBatches(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return list(new LambdaQueryWrapper<InventoryBatch>()
                .le(InventoryBatch::getExpiryDate, threshold)
                .eq(InventoryBatch::getStatus, 1)
                .gt(InventoryBatch::getQuantity, 0)
                .orderByAsc(InventoryBatch::getExpiryDate));
    }
}
