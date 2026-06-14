package com.phonestore.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String REGISTER_PATH = "/auth/register";
    public static final String LOGIN_PATH = "/auth/login";
    public static final String AUTH_BASE_PATH = "/auth/**";
    public static final String PHONES_PATH = "/phones/**";
    public static final long DEFAULT_JWT_EXPIRATION = 86400000;
}
