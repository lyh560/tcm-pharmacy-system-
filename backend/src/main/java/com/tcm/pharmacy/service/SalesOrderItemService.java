package com.tcm.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.pharmacy.entity.SalesOrderItem;
import com.tcm.pharmacy.mapper.SalesOrderItemMapper;
import org.springframework.stereotype.Service;

@Service
public class SalesOrderItemService extends ServiceImpl<SalesOrderItemMapper, SalesOrderItem> {
}
