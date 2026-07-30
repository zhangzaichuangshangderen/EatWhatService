package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.dto.NutritionGoalUpsertRequest;
import com.tencent.wxcloudrun.model.NutritionGoal;

public interface NutritionGoalService {

  NutritionGoal get(String userId);

  NutritionGoal upsert(String userId, NutritionGoalUpsertRequest request);
}
