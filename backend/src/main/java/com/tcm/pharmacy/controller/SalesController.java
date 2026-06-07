package com.tcm.pharmacy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.*;
import com.tcm.pharmacy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderItemService salesOrderItemService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Autowired
    private InventoryLogService inventoryLogService;

    @GetMapping("/page")
    public ApiResult<Page<SalesOrder>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status) {
        return ApiResult.success(salesOrderService.page(pageNum, pageSize, orderNo, status));
    }

    @PostMapping
    @Transactional
    public ApiResult<String> create(@RequestBody SalesOrderRequest request) {
        // 创建销售单
        SalesOrder order = new SalesOrder();
        order.setOrderNo(generateOrderNo("SO"));
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        order.setFinalAmount(BigDecimal.ZERO);
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
        order.setStatus("COMPLETED");
        salesOrderService.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 处理销售明细
        for (SalesOrderItemRequest item : request.getItems()) {
            // 检查库存
            Medicine medicine = medicineService.getById(item.getMedicineId());
            if (medicine == null) {
                throw new RuntimeException("药品不存在: " + item.getMedicineId());
            }
            if (medicine.getStockQuantity().compareTo(item.getQuantity()) < 0) {
                throw new RuntimeException("库存不足: " + medicine.getName()
                        + "，当前库存: " + medicine.getStockQuantity() + medicine.getUnit());
            }

            BigDecimal beforeQty = medicine.getStockQuantity();
            BigDecimal afterQty = beforeQty.subtract(item.getQuantity());

            // 扣减库存
            medicine.setStockQuantity(afterQty);
            medicineService.updateById(medicine);

            // 创建销售明细
            SalesOrderItem orderItem = new SalesOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setMedicineId(item.getMedicineId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setAmount(item.getQuantity().multiply(item.getPrice()));
            salesOrderItemService.save(orderItem);

            totalAmount = totalAmount.add(orderItem.getAmount());

            // 记录库存变动
            InventoryLog log = new InventoryLog();
            log.setMedicineId(item.getMedicineId());
            log.setType("SALE");
            log.setQuantity(item.getQuantity().negate());
            log.setBeforeQuantity(beforeQty);
            log.setAfterQuantity(afterQty);
            log.setOrderNo(order.getOrderNo());
            log.setRemark("销售出库");
            inventoryLogService.save(log);
        }

        // 更新订单金额
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount.subtract(order.getDiscount()));
        salesOrderService.updateById(order);

        return ApiResult.success(order.getOrderNo());
    }

    @GetMapping("/{id}")
    public ApiResult<SalesOrderDetail> getDetail(@PathVariable Long id) {
        SalesOrder order = salesOrderService.getById(id);
        if (order == null) {
            return ApiResult.error(404, "订单不存在");
        }

        List<SalesOrderItem> items = salesOrderItemService.list(
                new LambdaQueryWrapper<SalesOrderItem>().eq(SalesOrderItem::getOrderId, id));

        SalesOrderDetail detail = new SalesOrderDetail();
        detail.setOrder(order);
        detail.setItems(items);
        return ApiResult.success(detail);
    }

    private String generateOrderNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // 内部类
    public static class SalesOrderRequest {
        private String customerName;
        private String customerPhone;
        private BigDecimal discount;
        private String paymentMethod;
        private List<SalesOrderItemRequest> items;

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public List<SalesOrderItemRequest> getItems() { return items; }
        public void setItems(List<SalesOrderItemRequest> items) { this.items = items; }
    }

    public static class SalesOrderItemRequest {
        private Long medicineId;
        private BigDecimal quantity;
        private BigDecimal price;

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    public static class SalesOrderDetail {
        private SalesOrder order;
        private List<SalesOrderItem> items;

        public SalesOrder getOrder() { return order; }
        public void setOrder(SalesOrder order) { this.order = order; }
        public List<SalesOrderItem> getItems() { return items; }
        public void setItems(List<SalesOrderItem> items) { this.items = items; }
    }
}
