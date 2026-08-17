package com.tencent.wxcloudrun.dto;

import lombok.Data;

@Data
public class InviteCleanupResponse {
  private Boolean dryRun;
  private Boolean syncQualifiedFromDietRecords;
  private Integer dirtyNickNameCount;
  private Integer cleanedNickNameCount;
  private Integer canBeQualifiedCount;
  private Integer qualifiedSyncedCount;
}
