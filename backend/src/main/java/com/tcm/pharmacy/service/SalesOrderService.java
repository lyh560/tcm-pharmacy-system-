package com.tcm.pharmacy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.pharmacy.entity.SalesOrder;
import com.tcm.pharmacy.mapper.SalesOrderMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SalesOrderService extends ServiceImpl<SalesOrderMapper, SalesOrder> {

    public Page<SalesOrder> page(int pageNum, int pageSize, String orderNo, String status) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(SalesOrder::getOrderNo, orderNo);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SalesOrder::getStatus, status);
        }
        wrapper.orderByDesc(SalesOrder::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public BigDecimal getTodaySalesAmount() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<SalesOrder> orders = list(new LambdaQueryWrapper<SalesOrder>()
                .ge(SalesOrder::getCreateTime, start)
                .le(SalesOrder::getCreateTime, end)
                .eq(SalesOrder::getStatus, "COMPLETED"));
        return orders.stream()
                .map(SalesOrder::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
