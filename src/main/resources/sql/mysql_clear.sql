CREATE DATABASE IF NOT EXISTS `hzcu_mysql`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `hzcu_mysql`;

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

DROP VIEW IF EXISTS `v_monthly_usage_summary`;
DROP VIEW IF EXISTS `v_pending_orders`;
DROP VIEW IF EXISTS `v_item_summary`;
DROP VIEW IF EXISTS `v_user_profile`;

DROP PROCEDURE IF EXISTS `sp_user_order_report`;
DROP PROCEDURE IF EXISTS `sp_category_report`;
DROP PROCEDURE IF EXISTS `sp_monthly_report`;

DROP TRIGGER IF EXISTS `trg_orders_before_update`;
DROP TRIGGER IF EXISTS `trg_orders_before_insert`;
DROP TRIGGER IF EXISTS `trg_items_before_update`;
DROP TRIGGER IF EXISTS `trg_items_before_insert`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `profiles`;
DROP TABLE IF EXISTS `items`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;
SET FOREIGN_KEY_CHECKS = 1;
