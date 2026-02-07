# 开发指南

本文档为开发人员提供项目开发的详细说明。

## 项目架构

### 技术栈

**后端**
- Java 17
- Spring Boot 3.2.2
- MyBatis Plus 3.5.5
- Spring Security
- JWT
- MySQL + SQLite
- Redis

**前端**
- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- ECharts

### 目录结构

```
Greenly/
├── plant-backend/                 # 后端项目
│   ├── src/main/java/com/plant/backend/
│   │   ├── config/                # 配置类
│   │   │   ├── DataSourceConfig.java           # 双数据源配置
│   │   │   ├── PrimaryMyBatisConfig.java       # MySQL数据源配置
│   │   │   ├── SecondaryMyBatisConfig.java     # SQLite数据源配置
│   │   │   ├── SecurityConfig.java             # 安全配置
│   │   │   ├── MyBatisPlusConfig.java          # MyBatis Plus配置
│   │   │   └── RedisConfig.java                # Redis配置
│   │   ├── controller/            # 控制器层
│   │   │   ├── UserController.java
│   │   │   ├── PlantController.java
│   │   │   ├── CareScheduleController.java
│   │   │   ├── CareRecordController.java
│   │   │   ├── PhotoController.java
│   │   │   └── ReminderController.java
│   │   ├── service/               # 业务层
│   │   │   ├── UserService.java
│   │   │   ├── PlantService.java
│   │   │   ├── CareScheduleService.java
│   │   │   ├── CareRecordService.java
│   │   │   ├── PhotoService.java
│   │   │   └── ReminderService.java
│   │   ├── mapper/                # 数据访问层
│   │   │   ├── mysql/             # MySQL mapper
│   │   │   └── sqlite/            # SQLite mapper
│   │   ├── entity/                # 实体类
│   │   ├── dto/                   # 数据传输对象
│   │   ├── exception/             # 异常处理
│   │   ├── util/                  # 工具类
│   │   ├── task/                  # 定时任务
│   │   └── event/                 # 事件处理
│   └── src/main/resources/
│       ├── db/                    # 数据库脚本
│       │   ├── schema-mysql.sql   # MySQL建表脚本
│       │   └── schema-sqlite.sql  # SQLite建表脚本
│       └── application.yml        # 配置文件
│
└── plant-frontend/                # 前端项目
    ├── src/
    │   ├── api/                   # API接口封装
    │   │   ├── request.js         # axios实例配置
    │   │   ├── user.js            # 用户相关接口
    │   │   ├── plant.js           # 植物相关接口
    │   │   ├── care.js            # 养护相关接口
    │   │   ├── photo.js           # 照片相关接口
    │   │   └── reminder.js        # 提醒相关接口
    │   ├── assets/                # 静态资源
    │   ├── components/            # 公共组件
    │   ├── layout/                # 布局组件
    │   ├── pages/                 # 页面组件
    │   │   ├── login/             # 登录页
    │   │   ├── register/          # 注册页
    │   │   ├── dashboard/         # 首页
    │   │   ├── plant/             # 植物管理
    │   │   ├── care/              # 养护管理
    │   │   ├── photo/             # 相册管理
    │   │   ├── reminder/          # 提醒管理
    │   │   ├── user/              # 用户管理
    │   │   └── admin/             # 管理员功能
    │   ├── router/                # 路由配置
    │   ├── stores/                # 状态管理
    │   ├── App.vue                # 根组件
    │   └── main.js                # 入口文件
    └── package.json
```

## 开发规范

### 后端开发规范

#### 命名规范

- **类名**：大驼峰命名法，如 `UserController`
- **方法名**：小驼峰命名法，如 `getUserById`
- **常量名**：全大写下划线分隔，如 `DEFAULT_PAGE_SIZE`
- **变量名**：小驼峰命名法，如 `userName`

#### 代码风格

```java
// Controller示例
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
}

// Service示例
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}

// Entity示例
@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;
    private String password;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

#### 异常处理

```java
// 业务异常
throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");

// 全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
}
```

### 前端开发规范

#### 命名规范

- **组件名**：大驼峰命名法，如 `UserForm.vue`
- **变量名**：小驼峰命名法，如 `userName`
- **常量名**：全大写下划线分隔，如 `API_BASE_URL`

#### 代码风格

```vue
<!-- 组件示例 -->
<template>
  <div class="user-list">
    <el-table :data="tableData">
      <el-table-column prop="username" label="用户名" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList } from '@/api/user'

const tableData = ref([])

onMounted(() => {
  loadData()
})

const loadData = async () => {
  const res = await getUserList()
  tableData.value = res.data
}
</script>

<style scoped>
.user-list {
  padding: 20px;
}
</style>
```

#### API封装

```javascript
// api/request.js
import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => response.data,
  error => {
    // 错误处理
    return Promise.reject(error)
  }
)

export default request

// api/user.js
import request from './request'

export const login = (data) => {
  return request.post('/user/login', data)
}

export const getUserList = (params) => {
  return request.get('/user/list', { params })
}
```

## 数据库开发

### 双数据源使用

```java
// MySQL数据源（主数据源）
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 自动使用MySQL数据源
}

// SQLite数据源
@Mapper
public interface LocalPlantMapper extends BaseMapper<LocalPlant> {
    // 自动使用SQLite数据源
}
```

### 事务处理

```java
@Transactional(rollbackFor = Exception.class)
public void createOrder(Order order) {
    // 业务逻辑
}
```

注意：双数据源事务需要使用JTA或手动处理事务一致性。

## 常用命令

### 后端开发

```bash
# 启动开发服务器
mvn spring-boot:run

# 打包
mvn clean package -DskipTests

# 运行测试
mvn test

# 代码格式化
mvn spring-javaformat:apply
```

### 前端开发

```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 代码检查
npm run lint

# 代码格式化
npm run format
```

## 调试技巧

### 后端调试

1. 使用IDEA断点调试
2. 查看日志：`tail -f logs/plant-backend.log`
3. 开启MyBatis日志：在application.yml中配置

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 前端调试

1. 使用浏览器开发者工具
2. 使用Vue Devtools扩展
3. 查看Network面板监控API请求

## 版本管理

### Git分支策略

- `main`：主分支，稳定版本
- `develop`：开发分支
- `feature/*`：功能分支
- `hotfix/*`：紧急修复分支

### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具相关
```

## 相关文档

- [快速开始](QUICKSTART.md)
- [API文档](API.md)
- [部署指南](DEPLOYMENT.md)
- [数据库设计](DATABASE.md)
