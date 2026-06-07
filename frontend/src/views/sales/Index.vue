<template>
  <div class="sales-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>销售管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增销售
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="salesList" border style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="customerPhone" label="客户电话" width="120" />
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="优惠" width="80">
          <template #default="{ row }">
            ¥{{ row.discount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="finalAmount" label="实付金额" width="100">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.finalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            <el-tag :type="getPaymentTagType(row.paymentMethod)">
              {{ getPaymentText(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="销售时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
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

    <!-- 新增销售对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增销售"
      width="900px"
      @close="resetAddForm"
    >
      <el-form :model="addForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="客户姓名">
              <el-input v-model="addForm.customerName" placeholder="请输入客户姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户电话">
              <el-input v-model="addForm.customerPhone" placeholder="请输入客户电话" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支付方式">
              <el-select v-model="addForm.paymentMethod" placeholder="请选择">
                <el-option label="现金" value="CASH" />
                <el-option label="微信" value="WECHAT" />
                <el-option label="支付宝" value="ALIPAY" />
                <el-option label="刷卡" value="CARD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="优惠金额">
              <el-input-number v-model="addForm.discount" :min="0" :precision="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">销售明细</el-divider>

        <el-button type="primary" size="small" @click="addItem" class="mb-10">
          <el-icon><Plus /></el-icon>
          添加药品
        </el-button>

        <el-table :data="addForm.items" border>
          <el-table-column label="药品" width="200">
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
          <el-table-column label="库存" width="100">
            <template #default="{ row }">
              {{ row.stock || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.price" :min="0" :precision="2" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" size="small" />
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
        <el-button type="primary" @click="handleSubmitSale">提交</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ currentOrder.customerName }}</el-descriptions-item>
        <el-descriptions-item label="客户电话">{{ currentOrder.customerPhone }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ getPaymentText(currentOrder.paymentMethod) }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥{{ currentOrder.discount }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ currentOrder.finalAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(currentOrder.status) }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">销售明细</el-divider>

      <el-table :data="orderItems" border>
        <el-table-column prop="medicineId" label="药品ID" width="80" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const salesList = ref([])
const medicineList = ref([])
const orderItems = ref([])
const detailDialogVisible = ref(false)
const addDialogVisible = ref(false)
const currentOrder = ref({})

const searchForm = reactive({
  orderNo: '',
  customerName: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const addForm = reactive({
  customerName: '',
  customerPhone: '',
  paymentMethod: 'CASH',
  discount: 0,
  items: []
})

const loadData = async () => {
  try {
    const data = await request.get('/sales/page', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        orderNo: searchForm.orderNo
      }
    })
    salesList.value = data.records
    pagination.total = data.total
  } catch (error) {
    console.error('加载销售列表失败:', error)
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

const getPaymentTagType = (method) => {
  const types = { CASH: '', WECHAT: 'success', ALIPAY: 'primary', CARD: 'warning' }
  return types[method] || ''
}

const getPaymentText = (method) => {
  const texts = { CASH: '现金', WECHAT: '微信', ALIPAY: '支付宝', CARD: '刷卡' }
  return texts[method] || method
}

const getStatusTagType = (status) => {
  const types = { PENDING: 'warning', COMPLETED: 'success', REFUNDED: 'danger' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { PENDING: '待处理', COMPLETED: '已完成', REFUNDED: '已退款' }
  return texts[status] || status
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.customerName = ''
  handleSearch()
}

const handleAdd = () => {
  addDialogVisible.value = true
}

const resetAddForm = () => {
  addForm.customerName = ''
  addForm.customerPhone = ''
  addForm.paymentMethod = 'CASH'
  addForm.discount = 0
  addForm.items = []
}

const addItem = () => {
  addForm.items.push({
    medicineId: null,
    price: 0,
    quantity: 1,
    stock: 0
  })
}

const removeItem = (index) => {
  addForm.items.splice(index, 1)
}

const onMedicineChange = (val, index) => {
  const medicine = medicineList.value.find(m => m.id === val)
  if (medicine) {
    addForm.items[index].price = medicine.sellingPrice
    addForm.items[index].stock = medicine.stockQuantity
  }
}

const calculateTotal = () => {
  const total = addForm.items.reduce((sum, item) => {
    return sum + (item.price * item.quantity)
  }, 0)
  return (total - (addForm.discount || 0)).toFixed(2)
}

const handleSubmitSale = async () => {
  if (addForm.items.length === 0) {
    ElMessage.warning('请添加销售明细')
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
    await request.post('/sales', addForm)
    ElMessage.success('销售成功')
    addDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '销售失败')
  }
}

const handleView = async (row) => {
  try {
    const data = await request.get(`/sales/${row.id}`)
    currentOrder.value = data.order
    orderItems.value = data.items
    detailDialogVisible.value = true
  } catch (error) {
    console.error('加载订单详情失败:', error)
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
  loadMedicines()
})
</script>

<style scoped>
.sales-page {
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
