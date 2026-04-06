package com.app.userservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticateCredentialsRequest {

    private String service_id;
    private String service_secret;
}
