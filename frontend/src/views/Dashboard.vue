<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.medicineCount || 0 }}</div>
              <div class="stat-label">药品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.todaySalesAmount || 0 }}</div>
              <div class="stat-label">今日销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.lowStockCount || 0 }}</div>
              <div class="stat-label">库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.expiringCount || 0 }}</div>
              <div class="stat-label">近效期药品</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card>
          <template #header>
            <span>近7天出库量趋势</span>
          </template>
          <div ref="salesTrendChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>
            <span>各类别药材出库占比</span>
          </template>
          <div ref="categoryPieChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-row">
      <el-col :span="14">
        <el-card>
          <template #header>
            <span>库存不足药材列表</span>
          </template>
          <el-table :data="lowStockList" stripe style="width: 100%">
            <el-table-column prop="code" label="编码" width="100" />
            <el-table-column prop="name" label="名称" width="120" />
            <el-table-column prop="stockQuantity" label="当前库存" width="100">
              <template #default="{ row }">
                <span style="color: #f56c6c; font-weight: bold">{{ row.stockQuantity }}{{ row.unit }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="minStock" label="预警库存" width="100">
              <template #default="{ row }">
                {{ row.minStock }}{{ row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="origin" label="产地" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>
            <span>月度销售统计</span>
          </template>
          <div class="monthly-stats">
            <div class="monthly-item">
              <div class="monthly-label">本月销售总额</div>
              <div class="monthly-value">¥{{ monthlySales.totalAmount || 0 }}</div>
            </div>
            <div class="monthly-item">
              <div class="monthly-label">本月订单数</div>
              <div class="monthly-value">{{ monthlySales.orderCount || 0 }}单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Box, Money, Warning, Clock } from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const stats = ref({})
const lowStockList = ref([])
const monthlySales = ref({})
const salesTrendChart = ref(null)
const categoryPieChart = ref(null)

let salesChart = null
let categoryChart = null

const fetchStats = async () => {
  try {
    const data = await request.get('/dashboard/stats')
    stats.value = data
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

const fetchSalesTrend = async () => {
  try {
    const data = await request.get('/dashboard/sales-trend')
    await nextTick()
    if (salesTrendChart.value) {
      salesChart = echarts.init(salesTrendChart.value)
      salesChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.dates },
        yAxis: { type: 'value', name: '金额(元)' },
        series: [{
          data: data.amounts,
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.3 },
          itemStyle: { color: '#409eff' }
        }]
      })
    }
  } catch (e) {
    console.error('获取销售趋势失败:', e)
  }
}

const fetchCategorySales = async () => {
  try {
    const data = await request.get('/dashboard/category-sales')
    await nextTick()
    if (categoryPieChart.value) {
      categoryChart = echarts.init(categoryPieChart.value)
      categoryChart.setOption({
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie',
          radius: '60%',
          data: data.data,
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
        }]
      })
    }
  } catch (e) {
    console.error('获取分类销售失败:', e)
  }
}

const fetchLowStock = async () => {
  try {
    const data = await request.get('/dashboard/low-stock-list')
    lowStockList.value = data
  } catch (e) {
    console.error('获取库存预警失败:', e)
  }
}

const fetchMonthlySales = async () => {
  try {
    const data = await request.get('/dashboard/monthly-sales')
    monthlySales.value = data
  } catch (e) {
    console.error('获取月度销售失败:', e)
  }
}

onMounted(() => {
  fetchStats()
  fetchSalesTrend()
  fetchCategorySales()
  fetchLowStock()
  fetchMonthlySales()

  window.addEventListener('resize', () => {
    salesChart?.resize()
    categoryChart?.resize()
  })
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon .el-icon {
  font-size: 30px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.table-row {
  margin-bottom: 20px;
}

.monthly-stats {
  padding: 20px;
}

.monthly-item {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.monthly-item:last-child {
  border-bottom: none;
}

.monthly-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.monthly-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}
</style>
