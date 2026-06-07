<template>
  <div class="purchase-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>采购管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增采购
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="采购单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入采购单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="purchaseList" border style="width: 100%">
        <el-table-column prop="orderNo" label="采购单号" width="180" />
        <el-table-column prop="supplierName" label="供应商" width="150" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="handleComplete(row)" v-if="row.status === 'PENDING'">完成入库</el-button>
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

    <!-- 新增采购对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增采购"
      width="1000px"
      @close="resetAddForm"
    >
      <el-form :model="addForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" required>
              <el-select v-model="addForm.supplierId" placeholder="请选择供应商" filterable>
                <el-option
                  v-for="item in supplierList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注">
              <el-input v-model="addForm.remark" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">采购明细</el-divider>

        <el-button type="primary" size="small" @click="addItem" class="mb-10">
          <el-icon><Plus /></el-icon>
          添加药品
        </el-button>

        <el-table :data="addForm.items" border>
          <el-table-column label="药品" width="180">
            <template #default="{ row, $index }">
              <el-select v-model="row.medicineId" placeholder="请选择药品" filterable @change="(val) => onMedicineChange(val, $index)">
                <el-option
                  v-for="item in medicineList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="批次号" width="120">
            <template #default="{ row }">
              <el-input v-model="row.batchNo" placeholder="批次号" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="进价" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.price" :min="0" :precision="2" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="生产日期" width="150">
            <template #default="{ row }">
              <el-date-picker v-model="row.productionDate" type="date" placeholder="选择日期" size="small" value-format="YYYY-MM-DD" />
            </template>
          </el-table-column>
          <el-table-column label="过期日期" width="150">
            <template #default="{ row }">
              <el-date-picker v-model="row.expiryDate" type="date" placeholder="选择日期" size="small" value-format="YYYY-MM-DD" />
            </template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">
              ¥{{ (row.price * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" size="small" @click="removeItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="total-section">
          <span>总计: ¥{{ calculateTotal() }}</span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitPurchase">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const purchaseList = ref([])
const medicineList = ref([])
const supplierList = ref([])
const addDialogVisible = ref(false)

const searchForm = reactive({
  orderNo: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const addForm = reactive({
  supplierId: null,
  remark: '',
  items: []
})

const loadData = async () => {
  try {
    const data = await request.get('/purchase/page', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        orderNo: searchForm.orderNo,
        status: searchForm.status
      }
    })
    purchaseList.value = data.records
    pagination.total = data.total
  } catch (error) {
    console.error('加载采购列表失败:', error)
  }
}

const loadMedicines = async () => {
  try {
    const data = await request.get('/medicine/list')
    medicineList.value = data
  } catch (error) {
    console.error('加载药品列表失败:', error)
  }
}

const loadSuppliers = async () => {
  try {
    const data = await request.get('/supplier/list')
    supplierList.value = data
  } catch (error) {
    console.error('加载供应商列表失败:', error)
  }
}

const getStatusTagType = (status) => {
  const types = { PENDING: 'warning', COMPLETED: 'success', CANCELLED: 'danger' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { PENDING: '待处理', COMPLETED: '已完成', CANCELLED: '已取消' }
  return texts[status] || status
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  handleSearch()
}

const handleAdd = () => {
  addDialogVisible.value = true
}

const resetAddForm = () => {
  addForm.supplierId = null
  addForm.remark = ''
  addForm.items = []
}

const addItem = () => {
  addForm.items.push({
    medicineId: null,
    batchNo: '',
    price: 0,
    quantity: 1,
    productionDate: '',
    expiryDate: ''
  })
}

const removeItem = (index) => {
  addForm.items.splice(index, 1)
}

const onMedicineChange = (val, index) => {
  const medicine = medicineList.value.find(m => m.id === val)
  if (medicine) {
    addForm.items[index].price = medicine.purchasePrice
  }
}

const calculateTotal = () => {
  return addForm.items.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0).toFixed(2)
}

const handleSubmitPurchase = async () => {
  if (!addForm.supplierId) {
    ElMessage.warning('请选择供应商')
    return
  }
  if (addForm.items.length === 0) {
    ElMessage.warning('请添加采购明细')
    return
  }

  for (const item of addForm.items) {
    if (!item.medicineId) {
      ElMessage.warning('请选择药品')
      return
    }
    if (item.quantity <= 0) {
      ElMessage.warning('数量必须大于0')
      return
    }
  }

  try {
    await request.post('/purchase', addForm)
    ElMessage.success('采购单创建成功')
    addDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '创建失败')
  }
}

const handleComplete = (row) => {
  ElMessageBox.confirm('确认完成该采购单吗？完成后将自动入库。', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/purchase/${row.id}/complete`)
      ElMessage.success('采购单已完成，库存已更新')
      loadData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
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
  loadMedicines()
  loadSuppliers()
})
</script>

<style scoped>
.purchase-page {
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

.mb-10 {
  margin-bottom: 10px;
}

.amount-text {
  color: #f56c6c;
  font-weight: bold;
}

.total-section {
  margin-top: 20px;
  text-align: right;
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}
</style>
