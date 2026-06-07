<template>
  <div class="report-page">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 销售报表 -->
      <el-tab-pane label="销售报表" name="sales">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>销售统计</span>
              <div>
                <el-date-picker
                  v-model="salesDateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
                <el-button type="primary" class="ml-10" @click="loadSalesReport">查询</el-button>
              </div>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="销售总额" :value="salesStats.totalAmount" prefix="¥" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="订单数" :value="salesStats.orderCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="利润" :value="salesStats.profit" prefix="¥" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="客单价" :value="salesStats.avgOrderAmount" prefix="¥" />
            </el-col>
          </el-row>

          <el-divider />

          <div ref="salesChart" class="chart-container"></div>

          <el-divider content-position="left">销售明细</el-divider>

          <el-table :data="salesReportList" border>
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="orderCount" label="订单数" width="100" />
            <el-table-column prop="totalAmount" label="销售额" width="120">
              <template #default="{ row }">
                ¥{{ row.totalAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="profit" label="利润" width="120">
              <template #default="{ row }">
                ¥{{ row.profit }}
              </template>
            </el-table-column>
            <el-table-column prop="avgOrderAmount" label="客单价" width="120">
              <template #default="{ row }">
                ¥{{ row.avgOrderAmount }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 库存报表 -->
      <el-tab-pane label="库存报表" name="inventory">
        <el-card>
          <template #header>
            <span>库存统计</span>
          </template>

          <el-row :gutter="20" class="mb-20">
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic title="药品总数" :value="inventoryStats.totalMedicines" />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic title="库存预警数" :value="inventoryStats.warningCount" />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <el-statistic title="近效期药品" :value="inventoryStats.expiringCount" />
              </el-card>
            </el-col>
          </el-row>

          <el-divider />

          <div ref="inventoryChart" class="chart-container"></div>
        </el-card>
      </el-tab-pane>

      <!-- 药品销售排行 -->
      <el-tab-pane label="销售排行" name="ranking">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>药品销售排行</span>
              <el-select v-model="rankingPeriod" class="ml-10" @change="loadSalesRanking">
                <el-option label="今日" value="today" />
                <el-option label="本周" value="week" />
                <el-option label="本月" value="month" />
                <el-option label="本年" value="year" />
              </el-select>
            </div>
          </template>

          <el-table :data="salesRankingList" border>
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="medicineName" label="药品名称" width="150" />
            <el-table-column prop="specification" label="规格" width="120" />
            <el-table-column prop="salesQuantity" label="销售数量" width="100" />
            <el-table-column prop="salesAmount" label="销售金额" width="120">
              <template #default="{ row }">
                ¥{{ row.salesAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="percentage" label="占比" width="100">
              <template #default="{ row }">
                {{ row.percentage }}%
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const activeTab = ref('sales')
const salesDateRange = ref([])
const rankingPeriod = ref('month')
const salesChart = ref(null)
const inventoryChart = ref(null)

let salesChartInstance = null
let inventoryChartInstance = null

const salesStats = reactive({
  totalAmount: 0,
  orderCount: 0,
  profit: 0,
  avgOrderAmount: 0
})

const inventoryStats = reactive({
  totalMedicines: 0,
  warningCount: 0,
  expiringCount: 0
})

const salesReportList = ref([])
const salesRankingList = ref([])

const loadSalesReport = async () => {
  try {
    const params = {}
    if (salesDateRange.value && salesDateRange.value.length === 2) {
      params.startDate = salesDateRange.value[0]
      params.endDate = salesDateRange.value[1]
    }

    const data = await request.get('/report/sales', { params })
    salesStats.totalAmount = data.totalAmount || 0
    salesStats.orderCount = data.orderCount || 0
    salesStats.profit = data.profit || 0
    salesStats.avgOrderAmount = data.avgOrderAmount || 0
    salesReportList.value = data.details || []

    // 更新图表
    await nextTick()
    if (salesChart.value && salesReportList.value.length > 0) {
      salesChartInstance = echarts.init(salesChart.value)
      salesChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: salesReportList.value.map(d => d.date)
        },
        yAxis: { type: 'value', name: '金额(元)' },
        series: [{
          data: salesReportList.value.map(d => d.totalAmount),
          type: 'bar',
          itemStyle: { color: '#409eff' }
        }]
      })
    }
  } catch (error) {
    console.error('加载销售报表失败:', error)
  }
}

const loadInventoryReport = async () => {
  try {
    const statsData = await request.get('/dashboard/stats')
    inventoryStats.totalMedicines = statsData.medicineCount || 0
    inventoryStats.warningCount = statsData.lowStockCount || 0
    inventoryStats.expiringCount = statsData.expiringCount || 0

    // 获取分类销售数据
    const categoryData = await request.get('/dashboard/category-sales')
    await nextTick()
    if (inventoryChart.value) {
      inventoryChartInstance = echarts.init(inventoryChart.value)
      inventoryChartInstance.setOption({
        tooltip: { trigger: 'item' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: '60%',
          data: categoryData.data,
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
        }]
      })
    }
  } catch (error) {
    console.error('加载库存报表失败:', error)
  }
}

const loadSalesRanking = async () => {
  try {
    const data = await request.get('/report/sales-ranking', {
      params: { period: rankingPeriod.value }
    })
    salesRankingList.value = data || []
  } catch (error) {
    console.error('加载销售排行失败:', error)
  }
}

onMounted(() => {
  loadSalesReport()
  loadInventoryReport()
  loadSalesRanking()

  window.addEventListener('resize', () => {
    salesChartInstance?.resize()
    inventoryChartInstance?.resize()
  })
})
</script>

<style scoped>
.report-page {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ml-10 {
  margin-left: 10px;
}

.mb-20 {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
}
</style>
