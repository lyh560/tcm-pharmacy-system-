package com.tcm.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.pharmacy.entity.Medicine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicineMapper extends BaseMapper<Medicine> {
}
