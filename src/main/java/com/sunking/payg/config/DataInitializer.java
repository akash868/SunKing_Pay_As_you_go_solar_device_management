package com.sunking.payg.config;

import com.sunking.payg.entity.AppUser;
import com.sunking.payg.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    /**
     * Ensures default admin & agent users exist on startup (local/test profiles).
     * For production, users are seeded via Flyway V2__seed_data.sql.
     */
    @Bean
    @Profile({ "local", "test" })
    public CommandLineRunner initDefaultUsers(AppUserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                AppUser admin = AppUser.builder()
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("Admin@123"))
                        .fullName("Admin User")
                        .role(AppUser.Role.ADMIN)
                        .isActive(true)
                        .build();
                userRepository.save(admin);
                log.info("Created default admin user: admin / Admin@123");
            }

            if (!userRepository.existsByUsername("agent")) {
                AppUser agent = AppUser.builder()
                        .username("agent")
                        .passwordHash(passwordEncoder.encode("Admin@123"))
                        .fullName("Field Agent")
                        .role(AppUser.Role.AGENT)
                        .isActive(true)
                        .build();
                userRepository.save(agent);
                log.info("Created default agent user: agent / Admin@123");
            }
        };
    }
}
