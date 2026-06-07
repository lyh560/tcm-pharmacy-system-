<template>
  <div class="medicine-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>药品管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增药品
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="药品名称">
          <el-input v-model="searchForm.name" placeholder="请输入药品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="medicineList" border style="width: 100%">
        <el-table-column prop="code" label="药品编码" width="100" />
        <el-table-column prop="name" label="药品名称" width="120" />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="unit" label="单位" width="60" />
        <el-table-column prop="purchasePrice" label="进价" width="80">
          <template #default="{ row }">
            ¥{{ row.purchasePrice }}
          </template>
        </el-table-column>
        <el-table-column prop="sellingPrice" label="售价" width="80">
          <template #default="{ row }">
            ¥{{ row.sellingPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="库存" width="80">
          <template #default="{ row }">
            <span :class="{ 'warning-text': row.stockQuantity < row.minStock }">
              {{ row.stockQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="origin" label="产地" width="80" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="药品编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入药品编码" />
        </el-form-item>
        <el-form-item label="药品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-select v-model="form.unit" placeholder="请选择单位">
            <el-option label="克(g)" value="g" />
            <el-option label="千克(kg)" value="kg" />
            <el-option label="瓶" value="瓶" />
            <el-option label="盒" value="盒" />
          </el-select>
        </el-form-item>
        <el-form-item label="进价" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="售价" prop="sellingPrice">
          <el-input-number v-model="form.sellingPrice" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQuantity">
          <el-input-number v-model="form.stockQuantity" :min="0" />
        </el-form-item>
        <el-form-item label="最低库存" prop="minStock">
          <el-input-number v-model="form.minStock" :min="0" />
        </el-form-item>
        <el-form-item label="产地" prop="origin">
          <el-input v-model="form.origin" placeholder="请输入产地" />
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="form.manufacturer" placeholder="请输入生产厂家" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const medicineList = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增药品')
const formRef = ref(null)

const searchForm = reactive({
  name: '',
  categoryId: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  code: '',
  name: '',
  categoryId: '',
  specification: '',
  unit: 'g',
  purchasePrice: 0,
  sellingPrice: 0,
  stockQuantity: 0,
  minStock: 0,
  origin: '',
  manufacturer: '',
  status: 1
})

const rules = {
  code: [{ required: true, message: '请输入药品编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
  purchasePrice: [{ required: true, message: '请输入进价', trigger: 'blur' }],
  sellingPrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : ''
}

const loadData = async () => {
  try {
    const data = await request.get('/medicine/page', {
      params: {
        pageNum: pagination.page,
        pageSize: pagination.size,
        name: searchForm.name,
        categoryId: searchForm.categoryId || null
      }
    })
    medicineList.value = data.records
    pagination.total = data.total
  } catch (error) {
    console.error('加载药品列表失败:', error)
  }
}

const loadCategories = async () => {
  try {
    const data = await request.get('/category/list')
    categories.value = data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.categoryId = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增药品'
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    categoryId: '',
    specification: '',
    unit: 'g',
    purchasePrice: 0,
    sellingPrice: 0,
    stockQuantity: 0,
    minStock: 0,
    origin: '',
    manufacturer: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑药品'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该药品吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/medicine/${row.id}`)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (form.id) {
      await request.put('/medicine', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/medicine', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
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
  loadCategories()
})
</script>

<style scoped>
.medicine-page {
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
</style>
