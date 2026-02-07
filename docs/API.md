# API接口文档

## 通用说明

### 基础信息

- 基础URL：`http://localhost:8080/api`
- 数据格式：JSON
- 字符编码：UTF-8
- 认证方式：JWT Token

### 请求头

```
Content-Type: application/json
Authorization: Bearer {token}
```

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 认证接口

### 用户注册

**POST** `/api/user/register`

**请求体：**
```json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGc...",
    "user": {
      "userId": 1,
      "username": "testuser",
      "email": "test@example.com",
      "role": "USER"
    }
  }
}
```

### 用户登录

**POST** `/api/user/login`

**请求体：**
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGc...",
    "user": {
      "userId": 1,
      "username": "testuser",
      "email": "test@example.com",
      "role": "USER"
    }
  }
}
```

---

## 用户接口

### 更新用户信息

**PUT** `/api/user/update`

**请求头：** 需要Authorization

**请求体：**
```json
{
  "userId": 1,
  "email": "new@example.com",
  "phone": "13900139000"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "new@example.com",
    "phone": "13900139000"
  }
}
```

### 删除用户（管理员）

**DELETE** `/api/user/delete/{userId}`

**请求头：** 需要Authorization，需要管理员权限

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 获取用户列表（管理员）

**GET** `/api/user/list`

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |
| keyword | string | 否 | 搜索关键词 |

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "userId": 1,
        "username": "testuser",
        "email": "test@example.com",
        "role": "USER",
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1
  }
}
```

---

## 植物接口

### 查询官方植物

**GET** `/api/plant/official/query`

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |
| keyword | string | 否 | 搜索关键词 |

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "绿萝",
        "genus": "绿萝属",
        "species": "绿萝",
        "description": "绿萝是天南星科绿萝属植物..."
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  }
}
```

### 查询我的植物

**GET** `/api/plant/local/list`

**请求头：** 需要Authorization

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | string | 否 | 搜索关键词 |

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "name": "我的绿萝",
      "genus": "绿萝属",
      "species": "绿萝",
      "description": "办公室的绿萝",
      "auditStatus": "LOCAL",
      "createTime": "2024-01-01T10:00:00"
    }
  ]
}
```

### 添加本地植物

**POST** `/api/plant/local/add`

**请求头：** 需要Authorization

**请求体：**
```json
{
  "userId": 1,
  "name": "我的绿萝",
  "genus": "绿萝属",
  "species": "绿萝",
  "description": "办公室的绿萝"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "我的绿萝",
    "genus": "绿萝属",
    "species": "绿萝",
    "description": "办公室的绿萝",
    "auditStatus": "LOCAL"
  }
}
```

### 提交审核

**POST** `/api/plant/audit/submit`

**请求头：** 需要Authorization

**请求体：**
```json
{
  "localPlantId": 1,
  "userId": 1
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "localPlantId": 1,
    "userId": 1,
    "status": "PENDING",
    "createTime": "2024-01-01T10:00:00"
  }
}
```

### 审核植物（管理员）

**POST** `/api/plant/audit/{auditOrderId}`

**请求头：** 需要Authorization，需要管理员权限

**请求体：**
```json
{
  "status": "APPROVED",
  "reviewerId": 1,
  "rejectReason": null
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 养护计划接口

### 查询养护计划

**GET** `/api/care/schedule/list`

**请求头：** 需要Authorization

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |
| plantId | long | 否 | 植物ID |
| status | int | 否 | 状态：0未完成，1已完成，2逾期 |

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "plantId": 1,
        "plantSource": "LOCAL",
        "taskName": "浇水",
        "dueTime": "2024-01-15T09:00:00",
        "status": 0,
        "createTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1
  }
}
```

### 添加养护计划

**POST** `/api/care/schedule/add`

**请求头：** 需要Authorization

**请求体：**
```json
{
  "userId": 1,
  "plantId": 1,
  "plantSource": "LOCAL",
  "taskName": "浇水",
  "dueTime": "2024-01-15T09:00:00",
  "reminderConfig": "{\"email\":true}"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "plantId": 1,
    "plantSource": "LOCAL",
    "taskName": "浇水",
    "dueTime": "2024-01-15T09:00:00",
    "status": 0
  }
}
```

### 更新养护计划

**PUT** `/api/care/schedule/update`

**请求头：** 需要Authorization

**请求体：**
```json
{
  "id": 1,
  "taskName": "浇水-每周一次",
  "dueTime": "2024-01-22T09:00:00",
  "status": 1
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "taskName": "浇水-每周一次",
    "dueTime": "2024-01-22T09:00:00",
    "status": 1
  }
}
```

### 删除养护计划

**DELETE** `/api/care/schedule/delete/{id}`

**请求头：** 需要Authorization

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 养护记录接口

### 查询养护记录

**GET** `/api/care/record/list`

**请求头：** 需要Authorization

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |
| plantId | long | 否 | 植物ID |
| plantSource | string | 否 | 植物来源 |

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "plantId": 1,
        "plantSource": "LOCAL",
        "recordTime": "2024-01-15T09:00:00",
        "operations": "{\"water\":200}",
        "remarks": "今天浇了200ml水"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1
  }
}
```

### 添加养护记录

**POST** `/api/care/record/add`

**请求头：** 需要Authorization

**请求体：**
```json
{
  "userId": 1,
  "plantId": 1,
  "plantSource": "LOCAL",
  "scheduleId": 1,
  "recordTime": "2024-01-15T09:00:00",
  "operations": "{\"water\":200}",
  "remarks": "今天浇了200ml水"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "plantId": 1,
    "plantSource": "LOCAL",
    "recordTime": "2024-01-15T09:00:00",
    "operations": "{\"water\":200}",
    "remarks": "今天浇了200ml水"
  }
}
```

### 养护数据统计

**GET** `/api/care/record/statistic`

**请求头：** 需要Authorization

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | long | 否 | 用户ID |
| plantId | long | 否 | 植物ID |
| days | int | 否 | 统计天数，默认30 |

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dates": ["2024-01-01", "2024-01-02", "2024-01-03"],
    "water": [200, 0, 150],
    "fertilizer": [0, 10, 0],
    "totalRecords": [1, 0, 1]
  }
}
```

---

## 照片接口

### 上传照片

**POST** `/api/photo/upload`

**请求头：** 需要Authorization，Content-Type: multipart/form-data

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 照片文件 |
| userId | long | 是 | 用户ID |
| plantId | long | 是 | 植物ID |
| plantSource | string | 是 | 植物来源 |
| isPublic | int | 否 | 是否公开，默认0 |
| remarks | string | 否 | 备注 |

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "plantId": 1,
    "plantSource": "LOCAL",
    "url": "/uploads/xxx.jpg",
    "captureTime": "2024-01-15T10:00:00"
  }
}
```

### 查询照片列表

**GET** `/api/photo/list`

**请求头：** 需要Authorization

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |
| userId | long | 否 | 用户ID |
| plantId | long | 否 | 植物ID |
| isPublic | int | 否 | 是否公开 |

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "plantId": 1,
        "plantSource": "LOCAL",
        "plantName": "我的绿萝",
        "url": "/uploads/xxx.jpg",
        "captureTime": "2024-01-15T10:00:00",
        "isPublic": 0
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1
  }
}
```

### 更新照片信息

**PUT** `/api/photo/update`

**请求头：** 需要Authorization

**请求体：**
```json
{
  "id": 1,
  "remarks": "今天的绿萝长得很好",
  "isPublic": 1
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "remarks": "今天的绿萝长得很好",
    "isPublic": 1
  }
}
```

### 删除照片

**DELETE** `/api/photo/delete/{id}`

**请求头：** 需要Authorization

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 提醒接口

### 查询未读提醒

**GET** `/api/reminder/unread`

**请求头：** 需要Authorization

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUnread": 3,
    "details": {
      "careSchedule": [],
      "plantAudit": [],
      "announcement": []
    }
  }
}
```

### 更新提醒配置

**POST** `/api/reminder/config`

**请求头：** 需要Authorization

**请求体：**
```json
{
  "email": "test@example.com",
  "popupEnabled": 1,
  "bellEnabled": 1,
  "sceneConfig": "{}"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "email": "test@example.com",
    "popupEnabled": 1,
    "bellEnabled": 1
  }
}
```

### 标记已读

**POST** `/api/reminder/read/{id}`

**请求头：** 需要Authorization

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 40001 | 参数错误 |
| 40002 | 用户已存在 |
| 40003 | 用户名或密码错误 |
| 40101 | 未登录 |
| 40102 | Token过期 |
| 40103 | Token无效 |
| 40301 | 无权限 |
| 40401 | 资源不存在 |
| 50001 | 系统错误 |

---

## 相关文档

- [快速开始](QUICKSTART.md)
- [开发指南](DEVELOPMENT.md)
- [部署指南](DEPLOYMENT.md)
