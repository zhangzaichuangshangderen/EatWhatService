package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class NutritionGoalUpsertRequest {

  private Double targetKcal;
  private String source;
  private Double bmrKcal;
  private Double tdeeKcal;
  private String goalType;
  private String gender;
  private Integer age;
  private Double height;
  private Double weight;
  private Integer occupationIndex;
  private Integer exerciseLevelIndex;
}
