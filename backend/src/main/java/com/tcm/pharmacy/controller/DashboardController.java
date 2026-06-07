package com.tcm.pharmacy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.*;
import com.tcm.pharmacy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderItemService salesOrderItemService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineCategoryService categoryService;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 药品总数
        stats.put("medicineCount", medicineService.count());

        // 今日销售额
        stats.put("todaySalesAmount", salesOrderService.getTodaySalesAmount());

        // 库存预警数量
        stats.put("lowStockCount", medicineService.getLowStockMedicines().size());

        // 近效期药品数量（30天内）
        stats.put("expiringCount", inventoryBatchService.getExpiringBatches(30).size());

        return ApiResult.success(stats);
    }

    @GetMapping("/sales-trend")
    public ApiResult<Map<String, Object>> getSalesTrend() {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.toString());

            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            List<SalesOrder> orders = salesOrderService.list(
                    new LambdaQueryWrapper<SalesOrder>()
                            .ge(SalesOrder::getCreateTime, start)
                            .le(SalesOrder::getCreateTime, end)
                            .eq(SalesOrder::getStatus, "COMPLETED"));

            BigDecimal dayAmount = orders.stream()
                    .map(SalesOrder::getFinalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            amounts.add(dayAmount);
        }

        result.put("dates", dates);
        result.put("amounts", amounts);
        return ApiResult.success(result);
    }

    @GetMapping("/category-sales")
    public ApiResult<Map<String, Object>> getCategorySales() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();

        // 获取所有分类
        List<MedicineCategory> categories = categoryService.list();

        // 获取最近30天的销售明细
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<SalesOrder> recentOrders = salesOrderService.list(
                new LambdaQueryWrapper<SalesOrder>()
                        .ge(SalesOrder::getCreateTime, thirtyDaysAgo)
                        .eq(SalesOrder::getStatus, "COMPLETED"));

        List<Long> orderIds = recentOrders.stream()
                .map(SalesOrder::getId)
                .collect(Collectors.toList());

        if (!orderIds.isEmpty()) {
            List<SalesOrderItem> items = salesOrderItemService.list(
                    new LambdaQueryWrapper<SalesOrderItem>()
                            .in(SalesOrderItem::getOrderId, orderIds));

            // 按药品分类统计销售量
            Map<Long, BigDecimal> categorySales = new HashMap<>();
            for (SalesOrderItem item : items) {
                Medicine medicine = medicineService.getById(item.getMedicineId());
                if (medicine != null && medicine.getCategoryId() != null) {
                    categorySales.merge(medicine.getCategoryId(), item.getQuantity(), BigDecimal::add);
                }
            }

            for (MedicineCategory category : categories) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("name", category.getName());
                entry.put("value", categorySales.getOrDefault(category.getId(), BigDecimal.ZERO));
                data.add(entry);
            }
        } else {
            for (MedicineCategory category : categories) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("name", category.getName());
                entry.put("value", BigDecimal.ZERO);
                data.add(entry);
            }
        }

        result.put("data", data);
        return ApiResult.success(result);
    }

    @GetMapping("/low-stock-list")
    public ApiResult<List<Medicine>> getLowStockList() {
        return ApiResult.success(medicineService.getLowStockMedicines());
    }

    @GetMapping("/monthly-sales")
    public ApiResult<Map<String, Object>> getMonthlySales() {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime monthEnd = LocalDateTime.of(today, LocalTime.MAX);

        List<SalesOrder> monthOrders = salesOrderService.list(
                new LambdaQueryWrapper<SalesOrder>()
                        .ge(SalesOrder::getCreateTime, monthStart)
                        .le(SalesOrder::getCreateTime, monthEnd)
                        .eq(SalesOrder::getStatus, "COMPLETED"));

        BigDecimal totalAmount = monthOrders.stream()
                .map(SalesOrder::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("totalAmount", totalAmount);
        result.put("orderCount", monthOrders.size());
        return ApiResult.success(result);
    }
}
