package com.forumieu.moderation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModerationRequest {
    private String text;
    private String userId;
    private String postId;
}
