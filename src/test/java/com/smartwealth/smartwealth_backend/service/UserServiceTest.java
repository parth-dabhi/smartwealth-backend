package com.smartwealth.smartwealth_backend.service;

import com.smartwealth.smartwealth_backend.dto.request.UserCreateRequest;
import com.smartwealth.smartwealth_backend.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // Rolls back after every test so DB stays clean
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {
        // GIVEN
        UserCreateRequest dto = UserCreateRequest.builder()
                .email("testuser@example.com")
                .fullName("Test User")
                .mobileNumber("9876543210")
                .password("Pass1234")
                .build();

        // WHEN
        UserResponse response = userService.createUser(dto).get();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isNotNull();
        assertThat(response.getCustomerId().length()).isEqualTo(8);
        assertThat(response.getEmail()).isEqualTo("testuser@example.com");
    }
}
