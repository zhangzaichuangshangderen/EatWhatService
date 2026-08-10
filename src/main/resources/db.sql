CREATE DATABASE IF NOT EXISTS eatwhat;
USE eatwhat;

CREATE TABLE `Ingredients` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `category` varchar(16) NOT NULL,
  `kcal` decimal(8,2) NOT NULL DEFAULT '0.00',
  `carbs` decimal(8,2) NOT NULL DEFAULT '0.00',
  `protein` decimal(8,2) NOT NULL DEFAULT '0.00',
  `fat` decimal(8,2) NOT NULL DEFAULT '0.00',
  `fiber` decimal(8,2) DEFAULT NULL,
  `unit` varchar(8) NOT NULL,
  `approxUnit` varchar(128) DEFAULT NULL,
  `userId` varchar(64) NOT NULL COMMENT '微信 openid',
  `isDeleted` tinyint(1) NOT NULL DEFAULT '0',
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_userId_isDeleted_updatedAt` (`userId`, `isDeleted`, `updatedAt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `DietRecords` (
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

CREATE TABLE `NutritionGoals` (
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

CREATE TABLE `Users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` varchar(64) NOT NULL COMMENT '微信 openid',
  `nickName` varchar(64) DEFAULT NULL,
  `avatarUrl` varchar(512) DEFAULT NULL,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `SiteMessages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` varchar(64) NOT NULL COMMENT '微信 openid',
  `title` varchar(128) NOT NULL,
  `content` varchar(2048) NOT NULL,
  `type` varchar(32) NOT NULL DEFAULT 'system',
  `isRead` tinyint(1) NOT NULL DEFAULT '0',
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_userId_isRead_createdAt` (`userId`, `isRead`, `createdAt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `UserInvites` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inviterUserId` varchar(64) NOT NULL COMMENT '邀请人 openid',
  `inviteeUserId` varchar(64) NOT NULL COMMENT '被邀请人 openid',
  `bindAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `isQualified` tinyint(1) NOT NULL DEFAULT '0',
  `qualifiedAt` timestamp NULL DEFAULT NULL,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inviteeUserId` (`inviteeUserId`),
  KEY `idx_inviterUserId_qualifiedAt` (`inviterUserId`, `isQualified`, `qualifiedAt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `Friends` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `requesterUserId` varchar(64) NOT NULL COMMENT '好友申请发起人 openid',
  `viewerUserId` varchar(64) NOT NULL COMMENT '好友申请接收人 openid',
  `status` varchar(16) NOT NULL DEFAULT 'pending' COMMENT 'pending|confirmed|rejected',
  `requestNote` varchar(255) DEFAULT NULL,
  `requestAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `confirmAt` timestamp NULL DEFAULT NULL,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_requester_viewer` (`requesterUserId`, `viewerUserId`),
  KEY `idx_viewer_status_requestAt` (`viewerUserId`, `status`, `requestAt`),
  KEY `idx_requester_status_requestAt` (`requesterUserId`, `status`, `requestAt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
