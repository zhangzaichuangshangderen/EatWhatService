package com.tencent.wxcloudrun.dao;

import com.tencent.wxcloudrun.model.DietRecord;
import com.tencent.wxcloudrun.model.DietRecordMonthEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DietRecordsMapper {

  List<DietRecord> listByDate(@Param("userId") String userId, @Param("recordDate") LocalDate recordDate);

  List<DietRecordMonthEntry> listMonthEntries(@Param("userId") String userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

  int upsert(DietRecord record);

  int deleteByMeal(@Param("userId") String userId, @Param("recordDate") LocalDate recordDate,
                   @Param("mealKey") String mealKey);
}
