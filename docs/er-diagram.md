# 中药房管理系统 - 数据库 ER 图

## 概览

本系统包含 10 张核心数据表，采用软删除策略，支持多角色权限管理。

## ER 图

```mermaid
erDiagram
    USER ||--o{ SALES_ORDER : "creates"
    USER ||--o{ PURCHASE_ORDER : "creates"
    USER ||--o{ INVENTORY_LOG : "operates"

    MEDICINE_CATEGORY ||--o{ MEDICINE : "contains"
    MEDICINE ||--o{ INVENTORY_BATCH : "has"
    MEDICINE ||--o{ SALES_ORDER_ITEM : "sold_in"
    MEDICINE ||--o{ PURCHASE_ORDER_ITEM : "purchased_in"
    MEDICINE ||--o{ INVENTORY_LOG : "tracked_in"

    SUPPLIER ||--o{ PURCHASE_ORDER : "supplies"

    SALES_ORDER ||--o{ SALES_ORDER_ITEM : "contains"
    PURCHASE_ORDER ||--o{ PURCHASE_ORDER_ITEM : "contains"

    INVENTORY_BATCH ||--o{ SALES_ORDER_ITEM : "sold_from"

    USER {
        bigint id PK "用户ID"
        varchar username UK "用户名"
        varchar password "密码(BCrypt)"
        varchar real_name "真实姓名"
        varchar phone "手机号"
        varchar role "角色: ADMIN/MANAGER/STAFF"
        tinyint status "状态: 0-禁用 1-启用"
        tinyint deleted "软删除: 0-未删除 1-已删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    MEDICINE_CATEGORY {
        bigint id PK "分类ID"
        varchar name "分类名称"
        bigint parent_id "父分类ID"
        int sort "排序"
        tinyint deleted "软删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    MEDICINE {
        bigint id PK "药品ID"
        varchar code UK "药品编码"
        varchar name "药品名称"
        bigint category_id FK "分类ID"
        varchar specification "规格"
        varchar unit "单位(g/kg/瓶/盒)"
        decimal purchase_price "进价"
        decimal selling_price "售价"
        decimal stock_quantity "当前库存量"
        decimal min_stock "预警库存量"
        decimal max_stock "最高库存"
        varchar origin "产地"
        varchar manufacturer "生产厂家"
        text description "说明"
        tinyint status "状态: 0-下架 1-上架"
        tinyint deleted "软删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    SUPPLIER {
        bigint id PK "供应商ID"
        varchar name "供应商名称"
        varchar contact "联系人"
        varchar phone "电话"
        varchar address "地址"
        tinyint status "状态"
        tinyint deleted "软删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    INVENTORY_BATCH {
        bigint id PK "批次ID"
        bigint medicine_id FK "药品ID"
        varchar batch_no "批次号"
        decimal quantity "数量"
        decimal purchase_price "进价"
        date production_date "生产日期"
        date expiry_date "过期日期"
        varchar supplier "供应商"
        tinyint status "状态: 0-过期 1-正常"
        tinyint deleted "软删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    PURCHASE_ORDER {
        bigint id PK "采购单ID"
        varchar order_no UK "采购单号"
        bigint supplier_id FK "供应商ID"
        decimal total_amount "总金额"
        varchar status "状态: PENDING/COMPLETED/CANCELLED"
        bigint operator_id "操作员ID"
        varchar remark "备注"
        tinyint deleted "软删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    PURCHASE_ORDER_ITEM {
        bigint id PK "明细ID"
        bigint order_id FK "采购单ID"
        bigint medicine_id FK "药品ID"
        decimal quantity "数量"
        decimal price "单价"
        decimal amount "金额"
        varchar batch_no "批次号"
        date production_date "生产日期"
        date expiry_date "过期日期"
        tinyint deleted "软删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    SALES_ORDER {
        bigint id PK "销售单ID"
        varchar order_no UK "销售单号"
        varchar customer_name "客户姓名"
        varchar customer_phone "客户电话"
        decimal total_amount "总金额"
        decimal discount "优惠金额"
        decimal final_amount "实付金额"
        varchar payment_method "支付方式: CASH/WECHAT/ALIPAY/CARD"
        varchar status "状态: PENDING/COMPLETED/REFUNDED"
        bigint operator_id "操作员ID"
        varchar remark "备注"
        tinyint deleted "软删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    SALES_ORDER_ITEM {
        bigint id PK "明细ID"
        bigint order_id FK "销售单ID"
        bigint medicine_id FK "药品ID"
        bigint batch_id FK "批次ID"
        decimal quantity "数量"
        decimal price "单价"
        decimal amount "金额"
        tinyint deleted "软删除"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
    }

    INVENTORY_LOG {
        bigint id PK "记录ID"
        bigint medicine_id FK "药品ID"
        bigint batch_id FK "批次ID"
        varchar type "类型: PURCHASE/SALE/ADJUST/RETURN"
        decimal quantity "变动数量(正数入库,负数出库)"
        decimal before_quantity "变动前数量"
        decimal after_quantity "变动后数量"
        varchar order_no "关联单号"
        bigint operator_id "操作员ID"
        varchar remark "备注"
        datetime create_time "创建时间"
    }
```

## 表关系说明

### 核心业务流程

1. **采购入库流程**
   - 创建采购单(PURCHASE_ORDER) → 添加采购明细(PURCHASE_ORDER_ITEM) → 完成入库
   - 入库时自动创建库存批次(INVENTORY_BATCH) → 记录库存变动(INVENTORY_LOG) → 更新药品库存(MEDICINE)

2. **销售出库流程**
   - 创建销售单(SALES_ORDER) → 添加销售明细(SALES_ORDER_ITEM)
   - 出库时自动扣减药品库存(MEDICINE) → 记录库存变动(INVENTORY_LOG)

3. **库存管理**
   - 库存批次(INVENTORY_BATCH)管理药品的批次信息，支持先进先出
   - 库存变动记录(INVENTORY_LOG)追踪所有库存变化

### 字段说明

- **软删除**: 所有业务表都使用 `deleted` 字段进行软删除，0表示未删除，1表示已删除
- **状态字段**: 使用整数表示状态，如 0-禁用/下架，1-启用/上架
- **金额字段**: 使用 DECIMAL(10,2) 或 DECIMAL(12,2) 存储，确保精度
- **时间字段**: 自动填充创建时间和更新时间

## 索引说明

### 主要索引

- `medicine.code` - 药品编码唯一索引
- `medicine.name` - 药品名称索引
- `medicine.category_id` - 分类索引
- `sales_order.order_no` - 销售单号唯一索引
- `sales_order.create_time` - 创建时间索引
- `purchase_order.order_no` - 采购单号唯一索引
- `inventory_batch.medicine_id` - 药品ID索引
- `inventory_batch.expiry_date` - 过期日期索引
- `inventory_log.medicine_id` - 药品ID索引
- `inventory_log.create_time` - 创建时间索引

## 初始数据

系统初始化时会创建：
- 1个管理员账户 (admin/123456)
- 7个药品分类 (根茎类、果实种子类、花叶类、全草类、矿物类、动物类、加工类)
- 示例药品数据
- 示例供应商数据
