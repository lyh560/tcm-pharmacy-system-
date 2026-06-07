package com.tcm.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medicine")
public class Medicine {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private Long categoryId;
    private String specification;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private BigDecimal stockQuantity;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private String origin;
    private String manufacturer;
    private String description;
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
