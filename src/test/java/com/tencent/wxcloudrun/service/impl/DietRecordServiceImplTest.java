package com.tencent.wxcloudrun.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.dao.DietRecordsMapper;
import com.tencent.wxcloudrun.dto.DietFoodItem;
import com.tencent.wxcloudrun.dto.DietNutrition;
import com.tencent.wxcloudrun.dto.DietRecordDayResponse;
import com.tencent.wxcloudrun.dto.DietRecordMonthResponse;
import com.tencent.wxcloudrun.dto.DietRecordUpsertRequest;
import com.tencent.wxcloudrun.model.DietRecord;
import com.tencent.wxcloudrun.model.DietRecordMonthEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DietRecordServiceImplTest {

  private DietRecordsMapper mapper;
  private DietRecordServiceImpl service;

  @BeforeEach
  void setUp() {
    mapper = mock(DietRecordsMapper.class);
    service = new DietRecordServiceImpl(mapper, new ObjectMapper());
  }

  @Test
  void emptyDayContainsFourOrderedNullSlots() {
    LocalDate date = LocalDate.of(2026, 7, 30);
    when(mapper.listByDate("user-a", date)).thenReturn(Collections.emptyList());

    DietRecordDayResponse response = service.getDay("user-a", date);

    assertEquals(4, response.getMeals().size());
    assertEquals("breakfast", response.getMeals().get(0).getMealKey());
    assertEquals("lunch", response.getMeals().get(1).getMealKey());
    assertEquals("snack", response.getMeals().get(2).getMealKey());
    assertEquals("dinner", response.getMeals().get(3).getMealKey());
    response.getMeals().forEach(slot -> assertNull(slot.getRecord()));
    verify(mapper).listByDate("user-a", date);
  }

  @Test
  void upsertAlwaysUsesTrustedUserAndPathMeal() {
    LocalDate date = LocalDate.of(2026, 7, 30);
    when(mapper.listByDate("user-a", date)).thenReturn(Collections.emptyList());
    DietRecordUpsertRequest request = validRequest();

    service.upsertMeal("user-a", date, "breakfast", request);

    ArgumentCaptor<DietRecord> captor = ArgumentCaptor.forClass(DietRecord.class);
    verify(mapper).upsert(captor.capture());
    assertEquals("user-a", captor.getValue().getUserId());
    assertEquals(date, captor.getValue().getRecordDate());
    assertEquals("breakfast", captor.getValue().getMealKey());
  }

  @Test
  void deleteAlwaysIncludesTrustedUserDateAndMeal() {
    LocalDate date = LocalDate.of(2026, 7, 30);
    when(mapper.listByDate("user-b", date)).thenReturn(Collections.emptyList());

    service.deleteMeal("user-b", date, "dinner");

    verify(mapper).deleteByMeal("user-b", date, "dinner");
  }

  @Test
  void monthSummaryGroupsDatesAndKeepsMealOrder() {
    DietRecordMonthEntry breakfast = monthEntry("2026-07-02", "breakfast");
    DietRecordMonthEntry dinner = monthEntry("2026-07-02", "dinner");
    DietRecordMonthEntry lunch = monthEntry("2026-07-20", "lunch");
    when(mapper.listMonthEntries("user-a", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1)))
      .thenReturn(Arrays.asList(breakfast, dinner, lunch));

    DietRecordMonthResponse response = service.getMonth("user-a", YearMonth.of(2026, 7));

    assertEquals("2026-07", response.getMonth());
    assertEquals(2, response.getDays().size());
    assertEquals(Arrays.asList("breakfast", "dinner"), response.getDays().get(0).getMealKeys());
    assertEquals("2026-07-20", response.getDays().get(1).getDate());
  }

  private DietRecordMonthEntry monthEntry(String date, String mealKey) {
    DietRecordMonthEntry entry = new DietRecordMonthEntry();
    entry.setRecordDate(LocalDate.parse(date));
    entry.setMealKey(mealKey);
    return entry;
  }

  private DietRecordUpsertRequest validRequest() {
    DietNutrition totals = new DietNutrition();
    totals.setKcal(400.0);
    totals.setCarbs(50.0);
    totals.setProtein(25.0);
    totals.setFat(10.0);
    totals.setFiber(5.0);

    DietFoodItem item = new DietFoodItem();
    item.setId("c1");
    item.setName("白米饭");
    item.setCategory("carbs");
    item.setUnit("g");
    item.setAmount(150.0);
    item.setKcal(116.0);
    item.setCarbs(25.9);
    item.setProtein(2.6);
    item.setFat(0.3);

    DietRecordUpsertRequest request = new DietRecordUpsertRequest();
    request.setId("log-1");
    request.setScore(89.4);
    request.setTotals(totals);
    request.setItems(Collections.singletonList(item));
    request.setAcceptedAt("2026-07-30T07:30:00.000Z");
    request.setDayGoalKcal(1800.0);
    return request;
  }
}
