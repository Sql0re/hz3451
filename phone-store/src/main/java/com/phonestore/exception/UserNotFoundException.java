package com.phonestore.exception;

public class UserNotFoundException extends RuntimeException {

    private final String username;

    public UserNotFoundException(String username) {
        super("User not found: " + username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
