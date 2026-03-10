package com.app.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * User profile data stored in JSONB format.
 * Contains additional user information like avatar, bio, phone and configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile implements Serializable {
    
    /**
     * URL of the user's avatar image
     */
    private String avatarUrl;
    
    /**
     * User's biography or description
     */
    private String bio;
    
    /**
     * User's phone number
     */
    private String phone;

    /**
     * User's educative institution name (if is necessary)
     */
    private String educativeInstitution;
    
    /**
     * User configuration settings
     */
    private UserConfig config;
    
    /**
     * Inner class for user configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserConfig implements Serializable {
        /**
         * User's preferred theme (light/dark)
         */
        private String theme;
    }
}
