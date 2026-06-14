package com.phonestore.exception;

public class PhoneNotFoundException extends RuntimeException {

    private final Long phoneId;

    public PhoneNotFoundException(Long phoneId) {
        super("Phone not found with id: " + phoneId);
        this.phoneId = phoneId;
    }

    public Long getPhoneId() {
        return phoneId;
    }
}
