package com.tcm.pharmacy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.*;
import com.tcm.pharmacy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase")
public class PurchaseController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseOrderItemService purchaseOrderItemService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Autowired
    private InventoryLogService inventoryLogService;

    @GetMapping("/page")
    public ApiResult<Page<PurchaseOrder>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status) {
        return ApiResult.success(purchaseOrderService.page(pageNum, pageSize, orderNo, status));
    }

    @PostMapping
    @Transactional
    public ApiResult<String> create(@RequestBody PurchaseOrderRequest request) {
        // 创建采购单
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo("PO"));
        order.setSupplierId(request.getSupplierId());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus("PENDING");
        purchaseOrderService.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 处理采购明细
        for (PurchaseOrderItemRequest item : request.getItems()) {
            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setMedicineId(item.getMedicineId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setAmount(item.getQuantity().multiply(item.getPrice()));
            orderItem.setBatchNo(item.getBatchNo());
            orderItem.setProductionDate(item.getProductionDate());
            orderItem.setExpiryDate(item.getExpiryDate());
            purchaseOrderItemService.save(orderItem);

            totalAmount = totalAmount.add(orderItem.getAmount());
        }

        order.setTotalAmount(totalAmount);
        purchaseOrderService.updateById(order);

        return ApiResult.success(order.getOrderNo());
    }

    @PostMapping("/{id}/complete")
    @Transactional
    public ApiResult<Boolean> complete(@PathVariable Long id) {
        PurchaseOrder order = purchaseOrderService.getById(id);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            return ApiResult.error(400, "采购单不存在或状态异常");
        }

        List<PurchaseOrderItem> items = purchaseOrderItemService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getOrderId, id));

        for (PurchaseOrderItem item : items) {
            // 更新药品库存
            Medicine medicine = medicineService.getById(item.getMedicineId());
            if (medicine != null) {
                BigDecimal beforeQty = medicine.getStockQuantity();
                medicine.setStockQuantity(beforeQty.add(item.getQuantity()));
                medicineService.updateById(medicine);

                // 创建库存批次
                InventoryBatch batch = new InventoryBatch();
                batch.setMedicineId(item.getMedicineId());
                batch.setBatchNo(item.getBatchNo() != null ? item.getBatchNo() : generateOrderNo("BT"));
                batch.setQuantity(item.getQuantity());
                batch.setPurchasePrice(item.getPrice());
                batch.setProductionDate(item.getProductionDate());
                batch.setExpiryDate(item.getExpiryDate());
                batch.setStatus(1);
                inventoryBatchService.save(batch);

                // 记录库存变动
                InventoryLog log = new InventoryLog();
                log.setMedicineId(item.getMedicineId());
                log.setBatchId(batch.getId());
                log.setType("PURCHASE");
                log.setQuantity(item.getQuantity());
                log.setBeforeQuantity(beforeQty);
                log.setAfterQuantity(medicine.getStockQuantity());
                log.setOrderNo(order.getOrderNo());
                log.setRemark("采购入库");
                inventoryLogService.save(log);
            }
        }

        order.setStatus("COMPLETED");
        purchaseOrderService.updateById(order);

        return ApiResult.success(true);
    }

    private String generateOrderNo(String prefix) {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // 内部类用于接收请求
    public static class PurchaseOrderRequest {
        private Long supplierId;
        private List<PurchaseOrderItemRequest> items;

        public Long getSupplierId() { return supplierId; }
        public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
        public List<PurchaseOrderItemRequest> getItems() { return items; }
        public void setItems(List<PurchaseOrderItemRequest> items) { this.items = items; }
    }

    public static class PurchaseOrderItemRequest {
        private Long medicineId;
        private BigDecimal quantity;
        private BigDecimal price;
        private String batchNo;
        private LocalDate productionDate;
        private LocalDate expiryDate;

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public LocalDate getProductionDate() { return productionDate; }
        public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    }
}
