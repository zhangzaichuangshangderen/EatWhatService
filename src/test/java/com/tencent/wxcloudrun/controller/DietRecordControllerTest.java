package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.DietNutrition;
import com.tencent.wxcloudrun.dto.DietRecordUpsertRequest;
import com.tencent.wxcloudrun.service.DietRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DietRecordControllerTest {

  private DietRecordService service;
  private DietRecordController controller;
  private MockHttpServletRequest loggedInRequest;

  @BeforeEach
  void setUp() {
    service = mock(DietRecordService.class);
    controller = new DietRecordController(service);
    loggedInRequest = new MockHttpServletRequest();
    loggedInRequest.addHeader("X-WX-OPENID", "user-a");
  }

  @Test
  void rejectsRequestWithoutTrustedOpenId() {
    ApiResponse response = controller.getDay("2026-07-30", new MockHttpServletRequest());

    assertEquals("未登录，请从小程序访问", response.getErrorMsg());
    verify(service, never()).getDay(any(), any());
  }

  @Test
  void rejectsInvalidDateAndMeal() {
    ApiResponse invalidDate = controller.getDay("2026-02-30", loggedInRequest);
    ApiResponse invalidMeal = controller.deleteMeal("2026-07-30", "brunch", loggedInRequest);

    assertEquals("日期格式错误，请使用yyyy-MM-dd", invalidDate.getErrorMsg());
    assertEquals("餐次错误", invalidMeal.getErrorMsg());
  }

  @Test
  void rejectsNonFiniteNutrition() {
    DietRecordUpsertRequest request = validRequest();
    request.getTotals().setKcal(Double.NaN);

    ApiResponse response = controller.upsertMeal("2026-07-30", "breakfast", request, loggedInRequest);

    assertEquals("总营养超出范围", response.getErrorMsg());
    verify(service, never()).upsertMeal(any(), any(), any(), any());
  }

  @Test
  void passesTrustedUserAndValidatedPathToService() {
    DietRecordUpsertRequest request = validRequest();

    controller.upsertMeal("2026-07-30", "snack", request, loggedInRequest);

    verify(service).upsertMeal(eq("user-a"), eq(LocalDate.of(2026, 7, 30)), eq("snack"), eq(request));
  }

  @Test
  void validatesMonthAndPassesTrustedUserToService() {
    ApiResponse unauthenticated = controller.getMonth("2026-07", new MockHttpServletRequest());
    ApiResponse invalidMonth = controller.getMonth("2026-13", loggedInRequest);

    assertEquals("未登录，请从小程序访问", unauthenticated.getErrorMsg());
    assertEquals("月份格式错误，请使用yyyy-MM", invalidMonth.getErrorMsg());
    controller.getMonth("2026-07", loggedInRequest);
    verify(service).getMonth("user-a", YearMonth.of(2026, 7));
  }

  private DietRecordUpsertRequest validRequest() {
    DietNutrition totals = new DietNutrition();
    totals.setKcal(300.0);
    totals.setCarbs(40.0);
    totals.setProtein(20.0);
    totals.setFat(8.0);
    totals.setFiber(4.0);

    DietRecordUpsertRequest request = new DietRecordUpsertRequest();
    request.setId("log-1");
    request.setScore(90.0);
    request.setTotals(totals);
    request.setItems(Collections.emptyList());
    request.setAcceptedAt("2026-07-30T07:30:00.000Z");
    request.setDayGoalKcal(1800.0);
    return request;
  }
}
