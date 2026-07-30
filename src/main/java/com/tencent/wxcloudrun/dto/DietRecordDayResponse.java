package com.tencent.wxcloudrun.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DietRecordDayResponse {

  private String date;
  private List<DietRecordSlotResponse> meals;
}
