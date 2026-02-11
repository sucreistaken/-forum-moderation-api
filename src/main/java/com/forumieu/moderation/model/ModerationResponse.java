package com.forumieu.moderation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationResponse {
    private boolean safe;
    private String category;
    private double confidence;
    private String reason;
    private String postId;
}
