USE `hzcu_mysql`;

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

DELIMITER //

CREATE TRIGGER `trg_items_before_insert`
BEFORE INSERT ON `items`
FOR EACH ROW
BEGIN
  IF NEW.`amount` <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'items.amount must be greater than 0';
  END IF;

  IF NEW.`status` NOT IN (0, 1) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'items.status must be 0 or 1';
  END IF;
END//

CREATE TRIGGER `trg_items_before_update`
BEFORE UPDATE ON `items`
FOR EACH ROW
BEGIN
  IF NEW.`amount` < 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'items.amount must be greater than or equal to 0';
  END IF;

  IF NEW.`status` NOT IN (0, 1) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'items.status must be 0 or 1';
  END IF;

  SET NEW.`updated_at` = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER `trg_orders_before_insert`
BEFORE INSERT ON `orders`
FOR EACH ROW
BEGIN
  IF NEW.`amount` <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'orders.amount must be greater than 0';
  END IF;

  IF NEW.`status` NOT IN (0, 1, 2) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'orders.status must be 0, 1 or 2';
  END IF;
END//

CREATE TRIGGER `trg_orders_before_update`
BEFORE UPDATE ON `orders`
FOR EACH ROW
BEGIN
  DECLARE `v_item_amount` DECIMAL(10,2);
  DECLARE `v_item_status` TINYINT;

  IF NEW.`amount` <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'orders.amount must be greater than 0';
  END IF;

  IF NEW.`status` NOT IN (0, 1, 2) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'orders.status must be 0, 1 or 2';
  END IF;

  IF OLD.`status` IN (1, 2) AND NEW.`status` <> OLD.`status` THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'completed or cancelled orders cannot change status';
  END IF;

  IF OLD.`status` = 0 AND NEW.`status` = 1 THEN
    SELECT i.`amount`, i.`status`
      INTO `v_item_amount`, `v_item_status`
      FROM `items` i
      WHERE i.`item_id` = NEW.`item_id`;

    IF `v_item_status` <> 1 THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'item is not available';
    END IF;

    IF `v_item_amount` < NEW.`amount` THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'item amount is not enough';
    END IF;
  END IF;
END//

DELIMITER ;
