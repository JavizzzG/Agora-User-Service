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
public class AuthCredentialsRequest {

    private UUID user_id; //Can be a String but in this case we are using the UUID type
    private String identifier; //Email
    private String password; //Password in plain text
    private String credential_type; // "password"

}
