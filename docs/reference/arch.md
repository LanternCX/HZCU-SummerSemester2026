# 献血管理系统总体架构

来源：原始架构图资料

![](assets/arch.svg)

## 架构文字提取

- Java Application
- JDBC / MongoDB Driver
- 数据访问层
- DAO Pattern：统一封装数据库访问
- MySQL DAO：PreparedStatement / Transaction
- MongoDB DAO：Document / Aggregation
- MySQL 8.0：用户、权限、核心业务、订单记录
- MongoDB 5.0：行为日志、评论、业务详情、系统日志
- MySQL 与 MongoDB 通过 ID 引用关联数据
- 核心业务落 MySQL，日志与半结构化内容落 MongoDB，应用层通过 DAO 隔离访问细节。
