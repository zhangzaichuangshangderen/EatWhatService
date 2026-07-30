CREATE TABLE IF NOT EXISTS `NutritionGoals` (
  `userId` varchar(64) NOT NULL COMMENT '微信 openid',
  `targetKcal` decimal(10,2) NOT NULL,
  `source` varchar(16) NOT NULL,
  `bmrKcal` decimal(10,2) DEFAULT NULL,
  `tdeeKcal` decimal(10,2) DEFAULT NULL,
  `goalType` varchar(16) NOT NULL DEFAULT 'maintain',
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
