-- 已有 eatwhat 库升级脚本：增加食材可选字段和按用户隔离的饮食记录表。
-- 执行前请先备份；若字段已存在，请跳过对应 ALTER TABLE 语句。
USE eatwhat;

ALTER TABLE `Ingredients`
  ADD COLUMN `fiber` decimal(8,2) DEFAULT NULL AFTER `fat`;

ALTER TABLE `Ingredients`
  ADD COLUMN `approxUnit` varchar(128) DEFAULT NULL AFTER `unit`;

CREATE TABLE IF NOT EXISTS `DietRecords` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` varchar(64) NOT NULL COMMENT '微信 openid',
  `recordDate` date NOT NULL,
  `mealKey` varchar(16) NOT NULL,
  `clientRecordId` varchar(64) NOT NULL,
  `score` decimal(6,2) NOT NULL,
  `kcal` decimal(10,2) NOT NULL DEFAULT '0.00',
  `carbs` decimal(10,2) NOT NULL DEFAULT '0.00',
  `protein` decimal(10,2) NOT NULL DEFAULT '0.00',
  `fat` decimal(10,2) NOT NULL DEFAULT '0.00',
  `fiber` decimal(10,2) NOT NULL DEFAULT '0.00',
  `itemsJson` mediumtext NOT NULL,
  `acceptedAt` varchar(40) NOT NULL,
  `dayGoalKcal` decimal(10,2) DEFAULT NULL,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date_meal` (`userId`, `recordDate`, `mealKey`),
  KEY `idx_user_date` (`userId`, `recordDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
