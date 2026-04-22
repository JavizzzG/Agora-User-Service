package com.app.userservice.controller;

import com.app.userservice.dto.GoogleCreateUserRequest;
import com.app.userservice.service.GoogleUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class GoogleUserController {

    private final GoogleUserService googleUserService;

    @PostMapping("/register/google")
    public ResponseEntity<UUID> registerGoogleUser(@Valid @RequestBody GoogleCreateUserRequest request) {
        UUID createdUserId = googleUserService.createGoogleUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserId);
    }
}
