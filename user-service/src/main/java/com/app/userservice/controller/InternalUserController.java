package com.app.userservice.controller;

import com.app.userservice.dto.InternalAiProfileResponse;
import com.app.userservice.dto.InternalUserSummaryResponse;
import com.app.userservice.dto.InternalUsersBatchRequest;
import com.app.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}/ai-profile")
    public ResponseEntity<InternalAiProfileResponse> getAiProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getInternalAiProfile(userId));
    }

    @PostMapping("/batch-summary")
    public ResponseEntity<List<InternalUserSummaryResponse>> getBatchSummary(
            @Valid @RequestBody InternalUsersBatchRequest request
    ) {
        return ResponseEntity.ok(userService.getInternalUserSummaries(request.getUserIds()));
    }
}
