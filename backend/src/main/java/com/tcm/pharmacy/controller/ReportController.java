package com.tcm.pharmacy.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcm.pharmacy.dto.ApiResult;
import com.tcm.pharmacy.entity.*;
import com.tcm.pharmacy.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderItemService salesOrderItemService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private MedicineCategoryService categoryService;

    @GetMapping("/sales")
    public ApiResult<Map<String, Object>> getSalesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(LocalTime.MAX) : LocalDateTime.now();

        List<SalesOrder> orders = salesOrderService.list(
                new LambdaQueryWrapper<SalesOrder>()
                        .ge(SalesOrder::getCreateTime, start)
                        .le(SalesOrder::getCreateTime, end)
                        .eq(SalesOrder::getStatus, "COMPLETED"));

        BigDecimal totalAmount = orders.stream().map(SalesOrder::getFinalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        int orderCount = orders.size();
        BigDecimal avgOrderAmount = orderCount > 0 ? totalAmount.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal profit = totalAmount.multiply(new BigDecimal("0.3")); // 假设30%利润率

        // 按日期分组
        Map<String, List<SalesOrder>> groupedByDate = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate().toString()));

        List<Map<String, Object>> details = new ArrayList<>();
        for (Map.Entry<String, List<SalesOrder>> entry : groupedByDate.entrySet()) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("date", entry.getKey());
            detail.put("orderCount", entry.getValue().size());
            BigDecimal dayAmount = entry.getValue().stream().map(SalesOrder::getFinalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            detail.put("totalAmount", dayAmount);
            detail.put("profit", dayAmount.multiply(new BigDecimal("0.3")));
            detail.put("avgOrderAmount", entry.getValue().size() > 0 ?
                    dayAmount.divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            details.add(detail);
        }

        details.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("orderCount", orderCount);
        result.put("profit", profit);
        result.put("avgOrderAmount", avgOrderAmount);
        result.put("details", details);

        return ApiResult.success(result);
    }

    @GetMapping("/sales-ranking")
    public ApiResult<List<Map<String, Object>>> getSalesRanking(
            @RequestParam(defaultValue = "month") String period) {

        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        switch (period) {
            case "today":
                start = LocalDate.now().atStartOfDay();
                break;
            case "week":
                start = LocalDate.now().minusDays(7).atStartOfDay();
                break;
            case "year":
                start = LocalDate.now().withDayOfYear(1).atStartOfDay();
                break;
            case "month":
            default:
                start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                break;
        }

        // 获取时间范围内的订单
        List<SalesOrder> orders = salesOrderService.list(
                new LambdaQueryWrapper<SalesOrder>()
                        .ge(SalesOrder::getCreateTime, start)
                        .le(SalesOrder::getCreateTime, end)
                        .eq(SalesOrder::getStatus, "COMPLETED"));

        List<Long> orderIds = orders.stream().map(SalesOrder::getId).collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }

        // 获取销售明细
        List<SalesOrderItem> items = salesOrderItemService.list(
                new LambdaQueryWrapper<SalesOrderItem>()
                        .in(SalesOrderItem::getOrderId, orderIds));

        // 按药品统计销售量和金额
        Map<Long, BigDecimal> quantityMap = new HashMap<>();
        Map<Long, BigDecimal> amountMap = new HashMap<>();

        for (SalesOrderItem item : items) {
            quantityMap.merge(item.getMedicineId(), item.getQuantity(), BigDecimal::add);
            amountMap.merge(item.getMedicineId(), item.getAmount(), BigDecimal::add);
        }

        // 计算总金额
        BigDecimal totalSalesAmount = amountMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        // 构建排行数据
        List<Map<String, Object>> rankingList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : amountMap.entrySet()) {
            Medicine medicine = medicineService.getById(entry.getKey());
            if (medicine == null) continue;

            Map<String, Object> rankItem = new HashMap<>();
            rankItem.put("medicineName", medicine.getName());
            rankItem.put("specification", medicine.getSpecification());
            rankItem.put("salesQuantity", quantityMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            rankItem.put("salesAmount", entry.getValue());

            BigDecimal percentage = totalSalesAmount.compareTo(BigDecimal.ZERO) > 0 ?
                    entry.getValue().multiply(new BigDecimal("100")).divide(totalSalesAmount, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            rankItem.put("percentage", percentage);

            rankingList.add(rankItem);
        }

        // 按销售额降序排序
        rankingList.sort((a, b) -> ((BigDecimal) b.get("salesAmount")).compareTo((BigDecimal) a.get("salesAmount")));

        return ApiResult.success(rankingList);
    }
}
