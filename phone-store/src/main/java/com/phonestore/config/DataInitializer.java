package com.phonestore.config;

import com.phonestore.entity.Role;
import com.phonestore.entity.User;
import com.phonestore.repository.RoleRepository;
import com.phonestore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@Profile("demo")
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            return;
        }

        Role userRole = roleRepository.save(new Role(null, "ROLE_USER"));
        Role adminRole = roleRepository.save(new Role(null, "ROLE_ADMIN"));

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@phonestore.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
    }
}
