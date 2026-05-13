package com.app.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GoogleCreateUserResponse {
    @JsonProperty("user_id")
    private UUID id;
}
