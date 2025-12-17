package com.smartwealth.smartwealth_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter valid email")
    String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must have exactly 10 digits")
    String mobileNumber;

    @NotBlank(message = "Full name is required")
    @Size(min=3, max=100, message = "Full name must be between 3 and 100 characters")
    String fullName;

    @NotBlank(message = "Password is required")
    @Size(min=8, max=15, message = "Password must be between 8 and 15 characters")
    String password;

//    RiskProfile riskProfile;  // optional: "MODERATE"
}
