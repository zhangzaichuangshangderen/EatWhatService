package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class InviteCleanupRequest {
  private Boolean dryRun;
  private Boolean syncQualifiedFromDietRecords;
}
