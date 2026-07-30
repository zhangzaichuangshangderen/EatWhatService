package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.NutritionGoalsMapper;
import com.tencent.wxcloudrun.dto.NutritionGoalUpsertRequest;
import com.tencent.wxcloudrun.model.NutritionGoal;
import com.tencent.wxcloudrun.service.NutritionGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NutritionGoalServiceImpl implements NutritionGoalService {

  private final NutritionGoalsMapper nutritionGoalsMapper;

  public NutritionGoalServiceImpl(@Autowired NutritionGoalsMapper nutritionGoalsMapper) {
    this.nutritionGoalsMapper = nutritionGoalsMapper;
  }

  @Override
  public NutritionGoal get(String userId) {
    return nutritionGoalsMapper.findByUserId(userId);
  }

  @Override
  @Transactional
  public NutritionGoal upsert(String userId, NutritionGoalUpsertRequest request) {
    NutritionGoal goal = new NutritionGoal();
    goal.setUserId(userId);
    goal.setTargetKcal(request.getTargetKcal());
    goal.setSource(request.getSource());
    goal.setBmrKcal(request.getBmrKcal());
    goal.setTdeeKcal(request.getTdeeKcal());
    goal.setGoalType(request.getGoalType());
    nutritionGoalsMapper.upsert(goal);
    return nutritionGoalsMapper.findByUserId(userId);
  }
}
