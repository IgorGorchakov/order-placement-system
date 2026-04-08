package com.example.ebus.search.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TripSearchRequestValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTripSearchRequest {
    String message() default "Invalid search request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

class TripSearchRequestValidator implements ConstraintValidator<ValidTripSearchRequest, TripSearchRequest> {
    @Override
    public boolean isValid(TripSearchRequest request, ConstraintValidatorContext context) {
        boolean valid = true;

        if (request.minPrice() != null && request.maxPrice() != null) {
            if (request.maxPrice().compareTo(request.minPrice()) < 0) {
                context.buildConstraintViolationWithTemplate("Max price must be greater than or equal to min price")
                    .addPropertyNode("maxPrice")
                    .addConstraintViolation();
                valid = false;
            }
        }

        if (request.departureAfter() != null && request.departureBefore() != null) {
            if (!request.departureBefore().isAfter(request.departureAfter())) {
                context.buildConstraintViolationWithTemplate("Departure before must be after departure after")
                    .addPropertyNode("departureBefore")
                    .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
