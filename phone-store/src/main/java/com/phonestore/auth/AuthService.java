package com.phonestore.auth;

import com.phonestore.dto.AuthResponseDto;
import com.phonestore.dto.LoginDto;
import com.phonestore.dto.RegisterDto;
import com.phonestore.entity.User;
import com.phonestore.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserService userService,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public AuthResponseDto register(RegisterDto registerDto) {
        if (userService.existsByUsername(registerDto.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userService.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = userService.register(registerDto);

        String token = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
    }

    public AuthResponseDto login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        String token = jwtService.generateToken(authentication);
        User user = userService.findByUsername(loginDto.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
    }

    public AuthResponseDto refresh(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        String newToken = jwtService.refreshAccessToken(refreshToken);
        User user = userService.findByUsername(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        return AuthResponseDto.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
    }
}
