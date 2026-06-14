package com.phonestore.validator;

import com.phonestore.dto.PhoneDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class PhoneValidator {

    private PhoneValidator() {}

    public static List<String> validate(PhoneDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getBrand() == null || dto.getBrand().trim().isEmpty()) {
            errors.add("Brand is required");
        }
        if (dto.getModel() == null || dto.getModel().trim().isEmpty()) {
            errors.add("Model is required");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Price must be greater than 0");
        }
        if (dto.getStorageGb() == null || dto.getStorageGb() <= 0) {
            errors.add("Storage must be a positive number");
        }
        if (dto.getRamGb() == null || dto.getRamGb() <= 0) {
            errors.add("RAM must be a positive number");
        }
        return errors;
    }
}
