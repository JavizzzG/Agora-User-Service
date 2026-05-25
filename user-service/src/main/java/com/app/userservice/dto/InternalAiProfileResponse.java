package com.app.userservice.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record InternalAiProfileResponse(
        UUID userId,
        boolean agenticMode,
        String retroStyle,
        String exigencyLevel,
        boolean weeklyReport,
        boolean sendEmailNotification
) {
}
