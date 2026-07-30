-- 已有 eatwhat 库升级脚本：为 Ingredients 增加用户隔离字段
USE eatwhat;

ALTER TABLE `Ingredients`
  ADD COLUMN `userId` varchar(64) NOT NULL DEFAULT '' COMMENT '微信 openid' AFTER `unit`;

ALTER TABLE `Ingredients`
  DROP INDEX `idx_isDeleted_updatedAt`,
  ADD KEY `idx_userId_isDeleted_updatedAt` (`userId`, `isDeleted`, `updatedAt`);

-- 可选：清理无 userId 的历史测试数据
-- DELETE FROM `Ingredients` WHERE `userId` = '';
