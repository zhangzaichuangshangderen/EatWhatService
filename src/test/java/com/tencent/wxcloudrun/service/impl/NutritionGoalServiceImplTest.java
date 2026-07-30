package com.tencent.wxcloudrun.service.impl;

import com.tencent.wxcloudrun.dao.NutritionGoalsMapper;
import com.tencent.wxcloudrun.dto.NutritionGoalUpsertRequest;
import com.tencent.wxcloudrun.model.NutritionGoal;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NutritionGoalServiceImplTest {

  @Test
  void upsertAlwaysUsesTrustedUser() {
    NutritionGoalsMapper mapper = mock(NutritionGoalsMapper.class);
    NutritionGoalServiceImpl service = new NutritionGoalServiceImpl(mapper);
    NutritionGoalUpsertRequest request = new NutritionGoalUpsertRequest();
    request.setTargetKcal(2000.0);
    request.setSource("manual");
    request.setGoalType("maintain");
    when(mapper.findByUserId("user-a")).thenReturn(new NutritionGoal());

    service.upsert("user-a", request);

    ArgumentCaptor<NutritionGoal> captor = ArgumentCaptor.forClass(NutritionGoal.class);
    verify(mapper).upsert(captor.capture());
    assertEquals("user-a", captor.getValue().getUserId());
    assertEquals(2000.0, captor.getValue().getTargetKcal());
  }
}
