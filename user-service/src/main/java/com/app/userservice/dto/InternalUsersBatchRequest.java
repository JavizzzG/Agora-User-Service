package com.app.userservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternalUsersBatchRequest {

    @NotEmpty(message = "userIds is required")
    @Size(max = 200, message = "userIds size must be <= 200")
    private List<UUID> userIds;
}
