-- 中药房库存与销售管理系统数据库

CREATE DATABASE IF NOT EXISTS tcm_pharmacy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tcm_pharmacy;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `role` VARCHAR(20) NOT NULL DEFAULT 'STAFF' COMMENT '角色: ADMIN, MANAGER, STAFF',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除: 0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 药品分类表
CREATE TABLE IF NOT EXISTS `medicine_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '药品分类表';

-- 药品信息表
CREATE TABLE IF NOT EXISTS `medicine` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '药品编码',
    `name` VARCHAR(100) NOT NULL COMMENT '药品名称',
    `category_id` BIGINT COMMENT '分类ID',
    `specification` VARCHAR(100) COMMENT '规格',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位(g/kg/瓶/盒)',
    `purchase_price` DECIMAL(10,2) NOT NULL COMMENT '进价',
    `selling_price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `stock_quantity` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '库存数量',
    `min_stock` DECIMAL(10,2) DEFAULT 0 COMMENT '最低库存警戒',
    `max_stock` DECIMAL(10,2) DEFAULT 0 COMMENT '最高库存',
    `origin` VARCHAR(50) COMMENT '产地',
    `manufacturer` VARCHAR(100) COMMENT '生产厂家',
    `description` TEXT COMMENT '说明',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_code` (`code`),
    INDEX `idx_name` (`name`),
    INDEX `idx_category` (`category_id`)
) COMMENT '药品信息表';

-- 库存批次表 (先进先出管理)
CREATE TABLE IF NOT EXISTS `inventory_batch` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `medicine_id` BIGINT NOT NULL COMMENT '药品ID',
    `batch_no` VARCHAR(50) NOT NULL COMMENT '批次号',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '数量',
    `purchase_price` DECIMAL(10,2) NOT NULL COMMENT '进价',
    `production_date` DATE COMMENT '生产日期',
    `expiry_date` DATE COMMENT '过期日期',
    `supplier` VARCHAR(100) COMMENT '供应商',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-过期 1-正常',
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_medicine` (`medicine_id`),
    INDEX `idx_expiry` (`expiry_date`)
) COMMENT '库存批次表';

-- 供应商表
CREATE TABLE IF NOT EXISTS `supplier` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
    `contact` VARCHAR(50) COMMENT '联系人',
    `phone` VARCHAR(20) COMMENT '电话',
    `address` VARCHAR(200) COMMENT '地址',
    `status` TINYINT NOT NULL DEFAULT 1,
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '供应商表';

-- 采购单表
CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '采购单号',
    `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '总金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/COMPLETED/CANCELLED',
    `operator_id` BIGINT COMMENT '操作员ID',
    `remark` VARCHAR(500) COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_supplier` (`supplier_id`)
) COMMENT '采购单表';

-- 采购明细表
CREATE TABLE IF NOT EXISTS `purchase_order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '采购单ID',
    `medicine_id` BIGINT NOT NULL COMMENT '药品ID',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '数量',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `batch_no` VARCHAR(50) COMMENT '批次号',
    `production_date` DATE COMMENT '生产日期',
    `expiry_date` DATE COMMENT '过期日期',
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_order` (`order_id`)
) COMMENT '采购明细表';

-- 销售单表
CREATE TABLE IF NOT EXISTS `sales_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '销售单号',
    `customer_name` VARCHAR(50) COMMENT '客户姓名',
    `customer_phone` VARCHAR(20) COMMENT '客户电话',
    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '总金额',
    `discount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    `final_amount` DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '实付金额',
    `payment_method` VARCHAR(20) NOT NULL DEFAULT 'CASH' COMMENT '支付方式: CASH/WECHAT/ALIPAY/CARD',
    `status` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT '状态: PENDING/COMPLETED/REFUNDED',
    `operator_id` BIGINT COMMENT '操作员ID',
    `remark` VARCHAR(500) COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_customer` (`customer_phone`)
) COMMENT '销售单表';

-- 销售明细表
CREATE TABLE IF NOT EXISTS `sales_order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL COMMENT '销售单ID',
    `medicine_id` BIGINT NOT NULL COMMENT '药品ID',
    `batch_id` BIGINT COMMENT '批次ID',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '数量',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
    `deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_order` (`order_id`),
    INDEX `idx_medicine` (`medicine_id`)
) COMMENT '销售明细表';

-- 库存变动记录表
CREATE TABLE IF NOT EXISTS `inventory_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `medicine_id` BIGINT NOT NULL COMMENT '药品ID',
    `batch_id` BIGINT COMMENT '批次ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: PURCHASE/SALE/ADJUST/RETURN',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '变动数量(正数入库,负数出库)',
    `before_quantity` DECIMAL(10,2) NOT NULL COMMENT '变动前数量',
    `after_quantity` DECIMAL(10,2) NOT NULL COMMENT '变动后数量',
    `order_no` VARCHAR(50) COMMENT '关联单号',
    `operator_id` BIGINT COMMENT '操作员ID',
    `remark` VARCHAR(200) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_medicine` (`medicine_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_create_time` (`create_time`)
) COMMENT '库存变动记录表';

-- 插入默认管理员账户
INSERT INTO `user` (`username`, `password`, `real_name`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN');

-- 插入药品分类
INSERT INTO `medicine_category` (`name`, `parent_id`, `sort`) VALUES
('根茎类', 0, 1),
('果实种子类', 0, 2),
('花叶类', 0, 3),
('全草类', 0, 4),
('矿物类', 0, 5),
('动物类', 0, 6),
('加工类', 0, 7);
