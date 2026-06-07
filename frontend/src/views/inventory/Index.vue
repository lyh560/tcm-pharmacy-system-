<template>
  <div class="inventory-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存管理</span>
          <div>
            <el-button type="warning" @click="handleExpiring">
              <el-icon><Clock /></el-icon>
              近效期提醒
            </el-button>
            <el-button type="primary" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="药品名称">
          <el-input v-model="searchForm.name" placeholder="请输入药品名称" clearable />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="库存不足" value="low" />
            <el-option label="库存过多" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="inventoryList" border style="width: 100%">
        <el-table-column prop="code" label="药品编码" width="120" />
        <el-table-column prop="name" label="药品名称" width="150" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="stockQuantity" label="当前库存" width="100">
          <template #default="{ row }">
            <span :class="getStockClass(row)">
              {{ row.stockQuantity }}{{ row.unit }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="最低库存" width="100">
          <template #default="{ row }">
            {{ row.minStock }}{{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="maxStock" label="最高库存" width="100">
          <template #default="{ row }">
            {{ row.maxStock }}{{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column label="库存状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStockTagType(row)">
              {{ getStockStatus(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="origin" label="产地" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewBatches(row)">查看批次</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="mt-20"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 批次详情对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="库存批次详情"
      width="900px"
    >
      <el-table :data="batchList" border>
        <el-table-column prop="batchNo" label="批次号" width="120" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="purchasePrice" label="进价" width="100">
          <template #default="{ row }">
            ¥{{ row.purchasePrice }}
          </template>
        </el-table-column>
        <el-table-column prop="productionDate" label="生产日期" width="120" />
        <el-table-column prop="expiryDate" label="过期日期" width="120">
          <template #default="{ row }">
            <span :class="{ 'warning-text': isExpiringSoon(row.expiryDate) }">
              {{ row.expiryDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="150" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '过期' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 近效期提醒对话框 -->
    <el-dialog
      v-model="expiringDialogVisible"
      title="近效期药品提醒（30天内）"
      width="800px"
    >
      <el-table :data="expiringList" border>
        <el-table-column prop="batchNo" label="批次号" width="120" />
        <el-table-column prop="medicineName" label="药品名称" width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="expiryDate" label="过期日期" width="120">
          <template #default="{ row }">
            <span class="danger-text">{{ row.expiryDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余天数" width="100">
          <template #default="{ row }">
            <span class="warning-text">{{ getDaysUntilExpiry(row.expiryDate) }}天</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh, Clock } from '@element-plus/icons-vue'
import request from '@/utils/request'

const inventoryList = ref([])
const batchList = ref([])
const expiringList = ref([])
const batchDialogVisible = ref(false)
const expiringDialogVisible = ref(false)

const searchForm = reactive({
  name: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  try {
    const data = await request.get('/medicine/page', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        name: searchForm.name
      }
    })
    inventoryList.value = data.records
    pagination.total = data.total
  } catch (error) {
    console.error('加载库存列表失败:', error)
  }
}

const getStockClass = (row) => {
  if (row.stockQuantity < row.minStock) return 'warning-text'
  if (row.stockQuantity > row.maxStock) return 'danger-text'
  return 'success-text'
}

const getStockTagType = (row) => {
  if (row.stockQuantity < row.minStock) return 'warning'
  if (row.stockQuantity > row.maxStock) return 'danger'
  return 'success'
}

const getStockStatus = (row) => {
  if (row.stockQuantity < row.minStock) return '库存不足'
  if (row.stockQuantity > row.maxStock) return '库存过多'
  return '正常'
}

const isExpiringSoon = (date) => {
  if (!date) return false
  const expiryDate = new Date(date)
  const now = new Date()
  const diffDays = Math.ceil((expiryDate - now) / (1000 * 60 * 60 * 24))
  return diffDays <= 30
}

const getDaysUntilExpiry = (date) => {
  if (!date) return 0
  const expiryDate = new Date(date)
  const now = new Date()
  return Math.ceil((expiryDate - now) / (1000 * 60 * 60 * 24))
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = ''
  handleSearch()
}

const handleRefresh = () => {
  loadData()
}

const handleViewBatches = async (row) => {
  try {
    const data = await request.get('/inventory/batches', {
      params: { medicineId: row.id }
    })
    batchList.value = data
    batchDialogVisible.value = true
  } catch (error) {
    console.error('加载批次信息失败:', error)
  }
}

const handleExpiring = async () => {
  try {
    const data = await request.get('/inventory/expiring', {
      params: { days: 30 }
    })
    expiringList.value = data
    expiringDialogVisible.value = true
  } catch (error) {
    console.error('加载近效期药品失败:', error)
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.inventory-page {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.warning-text {
  color: #e6a23c;
  font-weight: bold;
}

.danger-text {
  color: #f56c6c;
  font-weight: bold;
}

.success-text {
  color: #67c23a;
}
</style>
