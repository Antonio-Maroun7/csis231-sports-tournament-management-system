package com.antonio.sportstournament.security;

import com.antonio.sportstournament.user.Role;
import com.antonio.sportstournament.user.User;
import com.antonio.sportstournament.user.UserMapper;
import com.antonio.sportstournament.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first ADMIN at startup when no ADMIN exists yet and ADMIN_EMAIL / ADMIN_PASSWORD are set.
 * Needed because POST /api/users is ADMIN-only.
 */
@Component
public class AdminBootstrap implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final String name;
    private final String email;
    private final String password;

    /**
     * @param userRepository user table access
     * @param mapper         builds the admin entity
     * @param passwordEncoder hashes the bootstrap password
     * @param name           display name ({@code app.bootstrap.admin.name}, default "Administrator")
     * @param email          admin email ({@code app.bootstrap.admin.email}); blank disables the bootstrap
     * @param password       admin password ({@code app.bootstrap.admin.password}); blank disables the bootstrap
     */
    public AdminBootstrap(UserRepository userRepository, UserMapper mapper, PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap.admin.name:Administrator}") String name,
                          @Value("${app.bootstrap.admin.email:}") String email,
                          @Value("${app.bootstrap.admin.password:}") String password) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * Runs once at startup. Does nothing if the email or password setting is blank or an ADMIN already exists;
     * skips (with a warning) if the email belongs to another account; otherwise saves a new ADMIN
     * with a hashed password and 2FA on.
     *
     * @param args command-line arguments (not used)
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (email.isBlank() || password.isBlank() || userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        String adminEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            log.warn("Bootstrap admin skipped: an account with email {} already exists", adminEmail);
            return;
        }
        String adminName = name.isBlank() ? "Administrator" : name.trim();
        // the mapper sets twoFactorEnabled = true for ADMIN accounts
        User admin = mapper.toEntity(adminName, adminEmail, passwordEncoder.encode(password), Role.ADMIN);
        userRepository.save(admin);
        log.info("Bootstrap admin created: {}", adminEmail);
    }
}
