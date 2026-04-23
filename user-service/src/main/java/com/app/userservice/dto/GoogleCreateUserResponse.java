package com.app.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GoogleCreateUserResponse {
    private UUID id;
}
