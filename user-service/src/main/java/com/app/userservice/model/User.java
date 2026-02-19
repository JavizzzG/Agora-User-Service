package com.app.userservice.model;

import com.github.f4b6a3.uuid.UuidCreator;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * User entity representing the users table in the database.
 * Stores basic user information and profile data in JSONB format.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @Column(columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "is_admin", nullable = false)
    @Builder.Default
    private Boolean isAdmin = false;
    
    /**
     * User profile stored as JSONB in PostgreSQL.
     * Contains avatar_url, bio, phone, and config with theme.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "profile", columnDefinition = "jsonb")
    private UserProfile profile;
    
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    /**
     * Automatically set the creation timestamp before persisting
     */
    @PrePersist
    protected void onCreate() {
        if(this.id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }

        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
