package com.phonestore.service;

import com.phonestore.dto.RegisterDto;
import com.phonestore.dto.UserDto;
import com.phonestore.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User register(RegisterDto registerDto);
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<UserDto> findAllUsers();
    UserDto createUser(RegisterDto registerDto);
    void deleteUser(Long id);
}
