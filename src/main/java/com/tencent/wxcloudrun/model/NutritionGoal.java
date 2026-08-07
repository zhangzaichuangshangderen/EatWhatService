package com.tencent.wxcloudrun.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NutritionGoal {

  @JsonIgnore
  private String userId;

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
  private LocalDateTime updatedAt;
}
