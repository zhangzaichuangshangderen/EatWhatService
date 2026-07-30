package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class NutritionGoalUpsertRequest {

  private Double targetKcal;
  private String source;
  private Double bmrKcal;
  private Double tdeeKcal;
  private String goalType;
}
