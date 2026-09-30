package com.mams.config;

import com.mams.entity.Role;
import com.mams.entity.User;
import com.mams.repository.RoleRepository;
import com.mams.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty() && userRepository.findByEmail("admin@mams.local").isEmpty()) {
                Role adminRole = roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found. Ensure V2__seed_roles.sql ran."));
                
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@mams.local");
                admin.setPasswordHash(passwordEncoder.encode("admin123")); // Default password
                admin.setRole(adminRole);
                admin.setStatus("ACTIVE");
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                
                userRepository.save(admin);
                System.out.println("Default admin user created: admin / admin123");
            }
        };
    }
}
