package com.smartwealth.smartwealth_backend.dto.response.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
