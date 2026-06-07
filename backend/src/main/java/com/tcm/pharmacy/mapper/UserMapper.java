package com.tcm.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tcm.pharmacy.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
