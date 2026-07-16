USE `hzcu_mysql`;

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

DELIMITER //

-- 生成指定年月的分类用血报表，包括申请量、使用量和状态分布。
CREATE PROCEDURE `sp_monthly_report`(
  IN `p_year` INT,
  IN `p_month` INT
)
BEGIN
  SELECT
    c.`name` AS `category_name`,
    COUNT(*) AS `order_count`,
    COALESCE(SUM(CASE WHEN o.`status` = 1 THEN o.`amount` ELSE 0 END), 0) AS `used_amount`,
    SUM(CASE WHEN o.`status` = 0 THEN 1 ELSE 0 END) AS `pending_count`,
    SUM(CASE WHEN o.`status` = 1 THEN 1 ELSE 0 END) AS `completed_count`,
    SUM(CASE WHEN o.`status` = 2 THEN 1 ELSE 0 END) AS `cancelled_count`
  FROM `orders` o
  JOIN `items` i ON i.`item_id` = o.`item_id`
  JOIN `categories` c ON c.`category_id` = i.`category_id`
  WHERE o.`created_at` >= MAKEDATE(`p_year`, 1) + INTERVAL (`p_month` - 1) MONTH
    AND o.`created_at` < MAKEDATE(`p_year`, 1) + INTERVAL `p_month` MONTH
  GROUP BY c.`name`
  ORDER BY `used_amount` DESC;
END//

-- 统计指定分类的库存批次数和可用总量；参数为空时统计全部分类。
CREATE PROCEDURE `sp_category_report`(
  IN `p_category_id` BIGINT
)
BEGIN
  SELECT
    c.`category_id`,
    c.`name` AS `category_name`,
    COUNT(i.`item_id`) AS `item_count`,
    COALESCE(SUM(i.`amount`), 0) AS `available_amount`
  FROM `categories` c
  LEFT JOIN `items` i ON i.`category_id` = c.`category_id`
  WHERE `p_category_id` IS NULL OR c.`category_id` = `p_category_id`
  GROUP BY c.`category_id`, c.`name`
  ORDER BY c.`category_id`;
END//

-- 统计指定用户在时间范围内的申请数量、完成用量和状态分布。
CREATE PROCEDURE `sp_user_order_report`(
  IN `p_user_id` BIGINT,
  IN `p_started_at` DATETIME,
  IN `p_ended_at` DATETIME
)
BEGIN
  SELECT
    u.`user_id`,
    u.`username`,
    COUNT(o.`order_id`) AS `order_count`,
    COALESCE(SUM(CASE WHEN o.`status` = 1 THEN o.`amount` ELSE 0 END), 0) AS `completed_amount`,
    SUM(CASE WHEN o.`status` = 0 THEN 1 ELSE 0 END) AS `pending_count`,
    SUM(CASE WHEN o.`status` = 1 THEN 1 ELSE 0 END) AS `completed_count`,
    SUM(CASE WHEN o.`status` = 2 THEN 1 ELSE 0 END) AS `cancelled_count`
  FROM `users` u
  LEFT JOIN `orders` o ON o.`user_id` = u.`user_id`
    AND o.`created_at` >= `p_started_at`
    AND o.`created_at` < `p_ended_at`
  WHERE u.`user_id` = `p_user_id`
  GROUP BY u.`user_id`, u.`username`;
END//

DELIMITER ;
