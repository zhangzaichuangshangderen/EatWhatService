-- 给 NutritionGoals 表增加 BMR 测算输入字段，使性别/年龄/身高/体重/职业等级/运动等级能跨设备同步
ALTER TABLE `NutritionGoals`
  ADD COLUMN `gender` varchar(8) DEFAULT NULL COMMENT '性别 male/female' AFTER `goalType`,
  ADD COLUMN `age` int DEFAULT NULL COMMENT '年龄' AFTER `gender`,
  ADD COLUMN `height` decimal(10,2) DEFAULT NULL COMMENT '身高 cm' AFTER `age`,
  ADD COLUMN `weight` decimal(10,2) DEFAULT NULL COMMENT '体重 kg' AFTER `height`,
  ADD COLUMN `occupationIndex` int DEFAULT NULL COMMENT '职业/日常活动等级索引' AFTER `weight`,
  ADD COLUMN `exerciseLevelIndex` int DEFAULT NULL COMMENT '额外运动等级索引' AFTER `occupationIndex`;
