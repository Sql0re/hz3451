package com.phonestore.auth;

import com.phonestore.constants.SecurityConstants;
import com.phonestore.util.JwtUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtUtils jwtUtils;

    public JwtService(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtUtils.generateToken(userDetails.getUsername());
    }

    public String generateToken(String username) {
        return jwtUtils.generateToken(username);
    }

    public String generateRefreshToken(String username) {
        return jwtUtils.generateRefreshToken(username);
    }

    public String extractUsername(String token) {
        return jwtUtils.extractUsername(token);
    }

    public String extractTokenType(String token) {
        return jwtUtils.extractTokenType(token);
    }

    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }

    public String refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        String type = extractTokenType(refreshToken);
        if (!"refresh".equals(type)) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }
        String username = extractUsername(refreshToken);
        return generateToken(username);
    }

    public String extractTokenFromHeader(String header) {
        if (header != null && header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return header.substring(SecurityConstants.BEARER_PREFIX.length());
        }
        return null;
    }
}
