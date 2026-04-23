package com.app.userservice.service;

import com.app.userservice.dto.GoogleCreateUserRequest;
import com.app.userservice.dto.GoogleCreateUserResponse;
import com.app.userservice.exception.DuplicateEmailException;
import com.app.userservice.model.User;
import com.app.userservice.model.UserProfile;
import com.app.userservice.repository.UserRepository;
import com.app.userservice.util.DataSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleUserService {

    private final UserRepository userRepository;
    private final DataSanitizer dataSanitizer;

    @Transactional
    public GoogleCreateUserResponse createGoogleUser(GoogleCreateUserRequest request) {
        log.info("Creating Google user with email: {}", request.getEmail());

        sanitizeRequest(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Attempted to create Google user with duplicate email: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .isAdmin(false)
                .profile(UserProfile.builder()
                        .avatarUrl(request.getAvatarUrl())
                        .build())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Successfully created Google user with id: {}", savedUser.getId());
        return new GoogleCreateUserResponse(savedUser.getId());
    }

    private void sanitizeRequest(GoogleCreateUserRequest request) {
        if (request.getFirstName() != null) {
            request.setFirstName(dataSanitizer.sanitizeName(request.getFirstName()));
        }

        if (request.getLastName() != null) {
            request.setLastName(dataSanitizer.sanitizeName(request.getLastName()));
        }

        if (request.getEmail() != null) {
            request.setEmail(dataSanitizer.sanitizeEmail(request.getEmail()));
        }

        if (request.getAvatarUrl() != null) {
            request.setAvatarUrl(dataSanitizer.sanitizeUrl(request.getAvatarUrl()));
        }
    }
}
