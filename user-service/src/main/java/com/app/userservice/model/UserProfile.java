package com.app.userservice.model;

import com.app.userservice.validation.ValidPhone;
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
    @ValidPhone
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

        /**
         * Notification when a student submits an activity.
         */
        private Boolean newSubmission;

        /**
         * Notification when a teacher grades your work.
         */
        private Boolean newGrading;

        /**
         * Notification one day before activity due date.
         */
        private Boolean submissionAlert;

        /**
         * Enable or disable email notifications.
         */
        private Boolean sendEmailNotification;

        /**
         * If true, AI can grade and provide feedback automatically on submission.
         */
        private Boolean agenticMode;

        /**
         * AI feedback detail style: brief, detailed, full.
         */
        private String retroStyle;

        /**
         * AI grading strictness level: flexible, moderated, strict.
         */
        private String exigencyLevel;

        /**
         * If true, AI generates weekly performance reports.
         */
        private Boolean weeklyReport;
    }
}
