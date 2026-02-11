package com.forumieu.moderation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationStats {
    private long totalRequests;
    private long flaggedCount;
    private long safeCount;
    private double flaggedPercentage;
}
