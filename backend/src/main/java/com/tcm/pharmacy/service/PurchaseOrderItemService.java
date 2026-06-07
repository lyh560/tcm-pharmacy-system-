package com.tcm.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.pharmacy.entity.PurchaseOrderItem;
import com.tcm.pharmacy.mapper.PurchaseOrderItemMapper;
import org.springframework.stereotype.Service;

@Service
public class PurchaseOrderItemService extends ServiceImpl<PurchaseOrderItemMapper, PurchaseOrderItem> {
}
