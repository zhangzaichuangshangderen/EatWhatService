package com.tencent.wxcloudrun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DietRecordSlotResponse {

  private String mealKey;
  private String mealLabel;
  private DietRecordResponse record;
}
