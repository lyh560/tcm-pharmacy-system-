package com.tcm.pharmacy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.pharmacy.entity.Medicine;
import com.tcm.pharmacy.mapper.MedicineMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MedicineService extends ServiceImpl<MedicineMapper, Medicine> {

    public Page<Medicine> page(int pageNum, int pageSize, String name, Long categoryId) {
        LambdaQueryWrapper<Medicine> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Medicine::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(Medicine::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Medicine::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<Medicine> getLowStockMedicines() {
        List<Medicine> all = list(new LambdaQueryWrapper<Medicine>()
                .eq(Medicine::getStatus, 1)
                .gt(Medicine::getMinStock, 0));
        return all.stream()
                .filter(m -> m.getStockQuantity().compareTo(m.getMinStock()) <= 0)
                .collect(java.util.stream.Collectors.toList());
    }
}
