package com.antonio.sportstournament.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Spring Security setup: the shared PasswordEncoder, a stateless JWT filter chain, coarse URL-level
 * role rules, and 401/403 answers written as ProblemDetail JSON.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String UNAUTHORIZED_BODY = "{\"type\":\"about:blank\",\"title\":\"Unauthorized\","
            + "\"status\":401,\"detail\":\"Your session has expired or you are not logged in. "
            + "Please log in again.\"}";
    private static final String FORBIDDEN_BODY = "{\"type\":\"about:blank\",\"title\":\"Forbidden\","
            + "\"status\":403,\"detail\":\"You do not have permission to do this.\"}";

    /**
     * Password hashing bean shared by registration, login, 2FA codes and the bootstrap admin.
     *
     * @return a BCrypt encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes Spring's authentication manager as a bean.
     *
     * @param configuration Spring Security's authentication configuration
     * @return the configured authentication manager
     * @throws Exception if Spring cannot build the manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Coarse, URL-level access rules; the first matching rule wins. Ownership rules need database
     * state, so they live in the services.
     *
     * @param http               the Spring Security builder
     * @param jwtService         used by the JWT filter to validate tokens
     * @param userDetailsService used by the JWT filter to load the user
     * @return the security filter chain
     * @throws Exception if the chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   UserDetailsService userDetailsService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)            // the token is in a header, there is no cookie
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login",
                                "/api/auth/verify-2fa", "/api/auth/resend-2fa").permitAll()
                        .requestMatchers("/error").permitAll()   // otherwise error pages turn into 401
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/two-factor").authenticated()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/teams").hasAnyRole("TEAM_MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tournaments").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/registrations/*/approve",
                                "/api/registrations/*/reject").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tournaments/*/registrations")
                                .hasAnyRole("TEAM_MANAGER", "ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, ex) ->
                                writeProblem(response, HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_BODY))
                        .accessDeniedHandler((request, response, ex) ->
                                writeProblem(response, HttpServletResponse.SC_FORBIDDEN, FORBIDDEN_BODY)))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeProblem(HttpServletResponse response, int status, String body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body);
    }
}
