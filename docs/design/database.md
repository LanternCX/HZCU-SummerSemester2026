# 数据库设计

本项目采用 MySQL + MongoDB 混合数据库。MySQL 保存用户、分类、核心业务、记录和档案等结构化数据；MongoDB 保存行为日志、评论、业务详情和系统日志等扩展数据。

## 设计文档

- [MySQL 数据库设计](mysql.md)
- [MongoDB 数据库设计](mongo.md)

## 数据库命名

| 类型 | 名称 |
| --- | --- |
| MySQL 数据库 | `hzcu_mysql` |
| MongoDB 数据库 | `hzcu_mongo` |

## 实现边界

同类流程采用统一实现方式，避免一部分流程放在存储过程，一部分流程放在 Java 代码中。

| 类型 | 职责 |
| --- | --- |
| Java/JDBC 事务 | 负责业务入口和写入流程，包括用户注册、登记库存、创建用血申请、审批用血申请、取消用血申请 |
| MySQL 视图 | 负责常用查询，不修改数据 |
| MySQL 存储过程 | 负责报表和统计，不承载创建、审批、取消等写入流程 |
| MySQL 触发器 | 负责兜底校验和自动维护，不隐藏核心业务动作 |
| MongoDB 写入 | 由 Java 层协调写入，不纳入 MySQL JDBC 事务 |
| MongoDB 聚合管道 | 负责日志、评论和详情数据的统计查询，不承载写入流程 |

## 配套脚本

- `src/main/resources/db.properties`：数据库连接配置。
- `src/main/resources/sql/mysql_clear.sql`：清空 MySQL 数据库对象。
- `src/main/resources/sql/mysql_schema.sql`：MySQL 建库、建表和索引。
- `src/main/resources/sql/mysql_views.sql`：MySQL 视图。
- `src/main/resources/sql/mysql_procedures.sql`：MySQL 存储过程。
- `src/main/resources/sql/mysql_triggers.sql`：MySQL 触发器。
- `src/main/resources/sql/mysql_init_data.sql`：MySQL Mock 数据。
- `src/main/resources/sql/mongodb_clear.js`：清空 MongoDB 集合和读模型。
- `src/main/resources/sql/mongodb_init.js`：MongoDB 集合、索引和读模型。
- `src/main/resources/sql/mongodb_init_data.js`：MongoDB Mock 数据。

## 初始化顺序

MySQL：

1. `mysql_clear.sql`
2. `mysql_schema.sql`
3. `mysql_views.sql`
4. `mysql_procedures.sql`
5. `mysql_triggers.sql`
6. `mysql_init_data.sql`

MongoDB：

1. `mongodb_clear.js`
2. `mongodb_init.js`
3. `mongodb_init_data.js`
