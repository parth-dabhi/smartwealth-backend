package com.smartwealth.smartwealth_backend.util;

import com.smartwealth.smartwealth_backend.annotation.Adult;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        if (dob == null) {
            return true; // @NotNull should handle null
        }

        return Period.between(dob, LocalDate.now()).getYears() >= 18;
    }
}
