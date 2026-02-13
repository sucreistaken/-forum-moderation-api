package com.forumieu.moderation.controller;

import com.forumieu.moderation.model.BatchModerationRequest;
import com.forumieu.moderation.model.ModerationRequest;
import com.forumieu.moderation.model.ModerationResponse;
import com.forumieu.moderation.model.ModerationStats;
import com.forumieu.moderation.service.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderate")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    @PostMapping("/text")
    public ResponseEntity<ModerationResponse> moderateText(@RequestBody ModerationRequest request) {
        ModerationResponse response = moderationService.moderateText(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ModerationResponse>> moderateBatch(@RequestBody BatchModerationRequest request) {
        List<ModerationResponse> responses = request.getItems().stream()
                .map(moderationService::moderateText)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stats")
    public ResponseEntity<ModerationStats> getStats() {
        return ResponseEntity.ok(moderationService.getStats());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Forum Moderation API is running");
    }
}
