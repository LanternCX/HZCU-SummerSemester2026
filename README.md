# 献血管理系统

献血管理系统是一个 Java Swing 桌面应用，用于管理用户档案、血液库存批次、
用血申请、评论、行为日志和统计报表。MySQL 保存核心业务数据，MongoDB 保存详情、
评论和日志等扩展数据。

## 技术栈

- Java 17
- Java Swing
- Maven
- JDBC + HikariCP
- MySQL 8.0
- MongoDB 5.0
- JUnit 5

## 环境要求

- JDK 17 或更高版本。
- Maven 3.9 或可兼容版本。
- MySQL 服务地址为 `localhost:3306`。
- MongoDB 服务地址为 `localhost:27017`。
- 桌面环境可正常显示 Swing 窗口。

仓库当前配置与 `hzcu-mysql`、`hzcu-mongo` 两个 Docker 容器匹配。

## 启动数据库

```bash
docker start hzcu-mysql hzcu-mongo
```

查看健康状态：

```bash
docker ps --filter name=hzcu-
```

数据库连接信息位于 `src/main/resources/db.properties`。如本地账号、密码或端口不同，在启动系统前修改该文件。

## 初始化 MySQL

按照以下顺序执行脚本：

```bash
docker exec -i hzcu-mysql mysql -uroot -p123456 < src/main/resources/sql/mysql_clear.sql
docker exec -i hzcu-mysql mysql -uroot -p123456 < src/main/resources/sql/mysql_schema.sql
docker exec -i hzcu-mysql mysql -uroot -p123456 < src/main/resources/sql/mysql_views.sql
docker exec -i hzcu-mysql mysql -uroot -p123456 < src/main/resources/sql/mysql_procedures.sql
docker exec -i hzcu-mysql mysql -uroot -p123456 < src/main/resources/sql/mysql_triggers.sql
docker exec -i hzcu-mysql mysql -uroot -p123456 < src/main/resources/sql/mysql_init_data.sql
```

`mysql_clear.sql` 会重建数据库，请勿对需要保留的数据执行。

## 初始化 MongoDB

```bash
docker exec -i hzcu-mongo mongosh \
  -u root -p 123456 --authenticationDatabase admin \
  < src/main/resources/sql/mongodb_clear.js
docker exec -i hzcu-mongo mongosh \
  -u root -p 123456 --authenticationDatabase admin \
  < src/main/resources/sql/mongodb_init.js
docker exec -i hzcu-mongo mongosh \
  -u root -p 123456 --authenticationDatabase admin \
  < src/main/resources/sql/mongodb_init_data.js
```

`mongodb_clear.js` 会清空项目集合，请勿对需要保留的数据执行。

## 运行系统

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.blooddonation.Main
```

如 Maven 环境未配置 `exec` 插件，可在 IDE 中直接运行 `com.blooddonation.Main`。

## 演示账号

| 角色 | 用户名 | 密码 | 用途 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `admin123` | 演示用户、分类、库存、订单、报表和日志管理 |
| 普通用户 | `user01` | `user123` | 演示库存查询、用血申请、评论和个人档案 |

账号 `auditor02` 为停用状态，可用于演示禁用账号无法登录。

## 测试

运行常规测试：

```bash
mvn test
```

运行 MongoDB 压力测试：

```bash
mvn -DstressTests=true -Dtest=SystemLogDAOStressTest test
```

压力测试会使用唯一运行标识写入 10000 条日志，验证数量后删除本次测试数据。

## 主要功能

- 用户注册、登录、状态和权限管理。
- 用户联系方式与实名档案维护。
- 血液分类和库存批次管理。
- 用血申请、审批、取消和库存扣减。
- 评论、评分和标签管理。
- 个性化库存批次推荐。
- 月度用血、操作热度、评论评分和用户行为报表。
- 登录日志、行为日志和操作审计。

## 项目文档

- [课程项目要求](docs/reference/req.md)
- [需求规格说明书](docs/design/requirements.md)
- [数据库设计](docs/design/database.md)
- [MySQL 数据库设计](docs/design/mysql.md)
- [MongoDB 数据库设计](docs/design/mongo.md)
- [用户手册](docs/用户手册.md)
- [性能优化报告](docs/性能优化报告.md)
- [测试报告](docs/测试报告.md)
- [答辩预演记录](docs/答辩预演记录.md)
- [代码审查自检清单](docs/代码审查自检清单.md)
- [项目总结报告](docs/项目总结报告.md)
- [UI 设计说明](docs/ui/design.md)

`docs/reference` 保存课程要求、讲义和案例资料，不作为项目实现说明。
