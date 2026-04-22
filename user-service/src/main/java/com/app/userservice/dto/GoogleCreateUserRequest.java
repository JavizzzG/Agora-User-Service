package com.app.userservice.dto;

import com.app.userservice.validation.NoSpecialCharacters;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleCreateUserRequest {

    @JsonProperty("name")
    @NotBlank(message = "Name is required")
    @Size(max = 30, message = "Name must be less than 30 characters")
    @NoSpecialCharacters(
            allowSpaces = true,
            allowHyphens = true,
            allowApostrophes = true,
            allowAccents = true,
            message = "Name contains invalid characters"
    )
    private String firstName;

    @JsonProperty("family_name")
    @NotBlank(message = "Family name is required")
    @Size(max = 30, message = "Family name must be less than 30 characters")
    @NoSpecialCharacters(
            allowSpaces = true,
            allowHyphens = true,
            allowApostrophes = true,
            allowAccents = true,
            message = "Family name contains invalid characters"
    )
    private String lastName;

    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @JsonProperty("picture")
    @NotBlank(message = "Picture is required")
    private String avatarUrl;
}
