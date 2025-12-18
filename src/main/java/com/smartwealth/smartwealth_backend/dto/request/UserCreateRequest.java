package com.smartwealth.smartwealth_backend.dto.request;

import com.smartwealth.smartwealth_backend.annotation.Adult;
import com.smartwealth.smartwealth_backend.entity.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

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

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Adult
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;
}
