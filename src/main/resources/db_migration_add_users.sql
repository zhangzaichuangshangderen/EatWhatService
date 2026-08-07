USE eatwhat;

CREATE TABLE IF NOT EXISTS `Users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` varchar(64) NOT NULL COMMENT '微信 openid',
  `nickName` varchar(64) DEFAULT NULL,
  `avatarUrl` varchar(512) DEFAULT NULL,
  `createdAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
