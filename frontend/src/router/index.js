import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/',
      component: () => import('@/layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'medicine',
          name: 'Medicine',
          component: () => import('@/views/medicine/Index.vue'),
          meta: { title: '药品管理' }
        },
        {
          path: 'inventory',
          name: 'Inventory',
          component: () => import('@/views/inventory/Index.vue'),
          meta: { title: '库存管理' }
        },
        {
          path: 'sales',
          name: 'Sales',
          component: () => import('@/views/sales/Index.vue'),
          meta: { title: '销售管理' }
        },
        {
          path: 'purchase',
          name: 'Purchase',
          component: () => import('@/views/purchase/Index.vue'),
          meta: { title: '采购管理', roles: ['ADMIN', 'MANAGER'] }
        },
        {
          path: 'report',
          name: 'Report',
          component: () => import('@/views/report/Index.vue'),
          meta: { title: '报表统计' }
        },
        {
          path: 'user',
          name: 'User',
          component: () => import('@/views/user/Index.vue'),
          meta: { title: '用户管理', roles: ['ADMIN'] }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 检查角色权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    const userRole = userInfo.role
    if (!to.meta.roles.includes(userRole)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
