# MongoDB 数据库设计

<!-- markdownlint-disable MD013 -->

## 设计目标

MongoDB 保存行为日志、评论、业务详情和系统日志等扩展数据，负责高写入日志、半结构化详情和聚合统计。

设计目标覆盖课程要求和献血管理系统的扩展数据场景：

- 支持登录日志、操作日志和行为追踪。
- 支持库存批次详情、评论、评分和标签。
- 支持嵌套文档和数组字段。
- 支持面向页面和报表的读模型。

## 集合设计

### action_logs 行为日志

记录用户在系统中的查询、浏览和操作行为。

```json
{
  "user_id": "10001",
  "item_id": "2001",
  "action_type": "VIEW",
  "duration_seconds": 120,
  "client_info": {
    "client_type": "DESKTOP",
    "ip": "127.0.0.1"
  },
  "created_at": "ISODate(...)"
}
```

索引：

- `user_id`, `created_at`
- `item_id`, `created_at`
- `action_type`, `created_at`

### comments 评论与互动

保存用户对库存批次或用血记录的评论、评分和标签。

```json
{
  "user_id": "10001",
  "item_id": "2001",
  "content": "资料完整",
  "rating": 5,
  "tags": ["审核", "完整"],
  "created_at": "ISODate(...)"
}
```

索引：

- `item_id`, `created_at`
- `user_id`, `created_at`
- `rating`

### item_details 业务详情

保存 MySQL `items` 表中血液库存批次的扩展详情。

```json
{
  "item_id": "2001",
  "description": "A 型红细胞库存批次",
  "images": [],
  "metadata": {
    "language": "zh-CN",
    "source": "献血登记"
  },
  "updated_at": "ISODate(...)"
}
```

索引：

- `item_id`
- `metadata.language`
- `updated_at`

### system_logs 系统操作日志

记录登录、异常和关键操作日志。

```json
{
  "user_id": "10001",
  "log_type": "LOGIN",
  "log_level": "INFO",
  "message": "登录成功",
  "action_detail": {
    "ip": "127.0.0.1",
    "operation": "POST /login"
  },
  "timestamp": "ISODate(...)"
}
```

索引：

- `user_id`, `timestamp`
- `log_type`, `timestamp`
- `log_level`, `timestamp`

## 读模型设计

MongoDB 聚合管道只用于读取和统计，不参与创建、审批、取消等写入流程。读模型按页面和报表设计，和 MySQL 视图、存储过程保持同一类职责：只读、汇总、辅助展示。

| 读模型 | 数据来源 | 实现方式 | 用途 |
| --- | --- | --- | --- |
| item_detail_view | `item_details` | 普通查询 | 查询库存批次的扩展详情 |
| item_comment_list | `comments` | 普通查询 | 查询库存批次或用血记录的评论列表 |
| agg_user_activity_report | `action_logs` | 聚合管道 | 用户行为报表，统计指定时间范围内每个用户的查看、查询和操作次数 |
| agg_item_attention_report | `action_logs`, `comments` | 聚合管道 | 核心业务数据关注度报表，统计每个核心业务数据的访问量、评论数和平均评分 |
| agg_comment_rating_report | `comments` | 聚合管道 | 评论评分报表，按库存批次统计评分分布和标签 |
| agg_system_audit_report | `system_logs` | 聚合管道 | 系统审计报表，按日志类型、日志级别和时间统计登录、异常和关键操作 |
| agg_login_risk_report | `system_logs` | 聚合管道 | 登录风险报表，按用户和 IP 统计失败登录、异常登录和高频登录 |

## 写入边界

MongoDB 写入由 Java 层协调，不纳入 MySQL JDBC 事务。

| 流程 | MongoDB 协调 |
| --- | --- |
| 登记血液库存 | 写入 `item_details` |
| 创建用血申请 | 写入 `action_logs` |
| 审批用血申请 | 写入 `system_logs` |
| 取消用血申请 | 写入 `system_logs` |

## Mock 数据要求

Mock 数据用于开发调试、功能演示和课程验收，不在设计文档中列出具体数据内容。

| 数据范围 | 要求 |
| --- | --- |
| 详情数据 | `item_details` 至少 20 条记录，并覆盖不同血型、血液成分和库存状态 |
| 评论数据 | `comments` 至少 20 条记录，覆盖不同评分、标签和关联对象 |
| 行为日志 | `action_logs` 至少 100 条记录，覆盖查看、查询、创建、审批等操作类型 |
| 系统日志 | `system_logs` 至少 100 条记录，覆盖登录、失败登录、异常和关键操作 |
| 报表覆盖 | Mock 数据必须能支撑 MongoDB 读模型查询出有效结果 |
| 数据安全 | Mock 数据不得使用真实身份证号、真实手机号、真实住址或真实患者信息 |
