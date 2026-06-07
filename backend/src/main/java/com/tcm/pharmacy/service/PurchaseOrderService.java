package com.tcm.pharmacy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.pharmacy.entity.PurchaseOrder;
import com.tcm.pharmacy.mapper.PurchaseOrderMapper;
import org.springframework.stereotype.Service;

@Service
public class PurchaseOrderService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {

    public Page<PurchaseOrder> page(int pageNum, int pageSize, String orderNo, String status) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(PurchaseOrder::getOrderNo, orderNo);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
