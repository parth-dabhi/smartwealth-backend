package com.smartwealth.smartwealth_backend.dto.request.family_member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FamilyAccessRequestDto {

    @Size(min=8, max=8, message = "CustomerId must be exactly 8 characters")
    @NotBlank(message = "CustomerId is required")
    String memberCustomerId;
}
