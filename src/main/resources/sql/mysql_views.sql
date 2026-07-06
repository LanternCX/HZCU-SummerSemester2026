USE `hzcu_mysql`;

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE VIEW `v_user_profile` AS
SELECT
  u.`user_id`,
  u.`username`,
  u.`email`,
  u.`phone`,
  u.`role`,
  u.`status`,
  p.`real_name`,
  p.`id_card`,
  p.`address`
FROM `users` u
LEFT JOIN `profiles` p ON p.`user_id` = u.`user_id`;

CREATE VIEW `v_item_summary` AS
SELECT
  i.`item_id`,
  i.`title`,
  c.`name` AS `category_name`,
  i.`amount` AS `available_amount`,
  i.`status` AS `item_status`,
  COUNT(o.`order_id`) AS `order_count`,
  COALESCE(SUM(CASE WHEN o.`status` = 1 THEN o.`amount` ELSE 0 END), 0) AS `used_amount`,
  MAX(o.`created_at`) AS `last_order_at`
FROM `items` i
JOIN `categories` c ON c.`category_id` = i.`category_id`
LEFT JOIN `orders` o ON o.`item_id` = i.`item_id`
GROUP BY i.`item_id`, i.`title`, c.`name`, i.`amount`, i.`status`;

CREATE VIEW `v_pending_orders` AS
SELECT
  o.`order_id`,
  u.`username`,
  o.`item_id`,
  i.`title`,
  o.`amount`,
  o.`created_at`
FROM `orders` o
JOIN `users` u ON u.`user_id` = o.`user_id`
JOIN `items` i ON i.`item_id` = o.`item_id`
WHERE o.`status` = 0;

CREATE VIEW `v_monthly_usage_summary` AS
SELECT
  DATE_FORMAT(o.`created_at`, '%Y-%m') AS `year_month`,
  c.`name` AS `category_name`,
  COUNT(*) AS `order_count`,
  COALESCE(SUM(CASE WHEN o.`status` = 1 THEN o.`amount` ELSE 0 END), 0) AS `used_amount`,
  SUM(CASE WHEN o.`status` = 1 THEN 1 ELSE 0 END) AS `completed_count`,
  SUM(CASE WHEN o.`status` = 2 THEN 1 ELSE 0 END) AS `cancelled_count`
FROM `orders` o
JOIN `items` i ON i.`item_id` = o.`item_id`
JOIN `categories` c ON c.`category_id` = i.`category_id`
GROUP BY DATE_FORMAT(o.`created_at`, '%Y-%m'), c.`name`;
