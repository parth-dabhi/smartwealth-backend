package com.smartwealth.smartwealth_backend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRequest {

    @Size(min=8, max=8, message = "CustomerId must be exactly 8 characters")
    @NotBlank(message = "CustomerId is required")
    String customerId;

    @NotBlank(message = "Password is required")
    @Size(min=8, max=15, message = "Password must be between 8 and 15 characters")
    String password;
}
