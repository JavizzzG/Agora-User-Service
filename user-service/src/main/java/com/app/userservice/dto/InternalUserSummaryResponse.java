package com.app.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserSummaryResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatarUrl;
}
