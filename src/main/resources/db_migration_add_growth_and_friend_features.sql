USE eatwhat;

CREATE TABLE IF NOT EXISTS `SiteMessages` (
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

CREATE TABLE IF NOT EXISTS `UserInvites` (
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

CREATE TABLE IF NOT EXISTS `Friends` (
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
