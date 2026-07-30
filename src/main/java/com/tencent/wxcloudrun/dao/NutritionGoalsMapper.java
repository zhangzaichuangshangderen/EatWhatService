package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.NutritionGoal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NutritionGoalsMapper {

  NutritionGoal findByUserId(@Param("userId") String userId);

  int upsert(NutritionGoal goal);
}
