package com.smartwealth.smartwealth_backend.controller;

import com.smartwealth.smartwealth_backend.dto.request.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.UserAuthResponse;
import com.smartwealth.smartwealth_backend.service.UserService;
import com.smartwealth.smartwealth_backend.api.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.API_USERS)
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Register a new user in the system.
     *
     * @param request user registration request
     * @return created user details
     */
    @PostMapping
    public ResponseEntity<UserAuthResponse> registerUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Received user registration request");
        UserAuthResponse response = userService.createUser(request)
                .orElseThrow(() -> new IllegalStateException("User creation failed"));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
