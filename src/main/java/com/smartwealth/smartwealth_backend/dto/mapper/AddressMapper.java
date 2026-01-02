package com.smartwealth.smartwealth_backend.dto.mapper;

import com.smartwealth.smartwealth_backend.dto.request.user.AddressRequest;
import com.smartwealth.smartwealth_backend.dto.response.user.AddressResponse;
import com.smartwealth.smartwealth_backend.entity.Address;

public class AddressMapper {
    private AddressMapper() {
    }

    public static Address toEntity(AddressRequest dto) {
        return Address.builder()
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .postalCode(dto.getPostalCode())
                .build();
    }

    public static AddressResponse toResponse(Address address) {
        if (address == null) {
            return null;
        }
        return AddressResponse.builder()
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .build();
    }
}
