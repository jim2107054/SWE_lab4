package com.example.webapp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        
                        // Course endpoints - Teachers can create, update, delete
                        .requestMatchers(HttpMethod.POST, "/api/courses/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("TEACHER")
                        
                        // View endpoints - Both roles can access
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").hasAnyRole("TEACHER", "STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/teachers/**").hasAnyRole("TEACHER", "STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/departments/**").hasAnyRole("TEACHER", "STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/students/**").hasAnyRole("TEACHER", "STUDENT")
                        
                        // Department management - Teachers only
                        .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("TEACHER")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
