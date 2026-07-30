package com.tencent.wxcloudrun.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DietRecordMonthEntry {

  private LocalDate recordDate;
  private String mealKey;
}
