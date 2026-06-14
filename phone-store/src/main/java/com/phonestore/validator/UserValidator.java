package com.phonestore.validator;

import com.phonestore.dto.RegisterDto;

import java.util.ArrayList;
import java.util.List;

public final class UserValidator {

    private UserValidator() {}

    public static List<String> validate(RegisterDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getUsername() == null || dto.getUsername().trim().length() < 3) {
            errors.add("Username must be at least 3 characters");
        }
        if (dto.getEmail() == null || !dto.getEmail().contains("@")) {
            errors.add("Invalid email format");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            errors.add("Password must be at least 6 characters");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            errors.add("Passwords do not match");
        }
        return errors;
    }
}
