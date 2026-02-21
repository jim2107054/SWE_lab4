package com.example.webapp.security;

import com.example.webapp.entity.Role;
import com.example.webapp.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        
        // Set the secret and expiration using reflection
        String secret = Base64.getEncoder().encodeToString(
                "mySecretKeyForJWTMustBeAtLeast256BitsLongForHS256Algorithm2024".getBytes());
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);

        Student student = new Student();
        student.setId(1L);
        student.setUsername("testuser");
        student.setPassword("password");
        student.setEmail("test@test.com");
        student.setName("Test User");
        student.setRole(Role.ROLE_STUDENT);
        student.setEnabled(true);

        userDetails = student;
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("should generate valid JWT token")
        void shouldGenerateValidJwtToken() {
            String token = jwtTokenProvider.generateToken(authentication);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }
    }

    @Nested
    @DisplayName("extractUsername")
    class ExtractUsername {

        @Test
        @DisplayName("should extract username from token")
        void shouldExtractUsernameFromToken() {
            String token = jwtTokenProvider.generateToken(authentication);

            String username = jwtTokenProvider.extractUsername(token);

            assertThat(username).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrueForValidToken() {
            String token = jwtTokenProvider.generateToken(authentication);

            boolean isValid = jwtTokenProvider.isTokenValid(token, userDetails);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("should return false for invalid username")
        void shouldReturnFalseForInvalidUsername() {
            String token = jwtTokenProvider.generateToken(authentication);

            Student differentUser = new Student();
            differentUser.setUsername("differentuser");
            differentUser.setRole(Role.ROLE_STUDENT);
            differentUser.setEnabled(true);

            boolean isValid = jwtTokenProvider.isTokenValid(token, differentUser);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should throw exception for malformed token")
        void shouldThrowExceptionForMalformedToken() {
            String invalidToken = "invalid.token.here";

            assertThatThrownBy(() -> jwtTokenProvider.isTokenValid(invalidToken, userDetails))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Token Expiration")
    class TokenExpiration {

        @Test
        @DisplayName("should create token with correct expiration")
        void shouldCreateTokenWithCorrectExpiration() {
            // Create a provider with short expiration for testing
            JwtTokenProvider shortExpiryProvider = new JwtTokenProvider();
            String secret = Base64.getEncoder().encodeToString(
                    "mySecretKeyForJWTMustBeAtLeast256BitsLongForHS256Algorithm2024".getBytes());
            ReflectionTestUtils.setField(shortExpiryProvider, "jwtSecret", secret);
            ReflectionTestUtils.setField(shortExpiryProvider, "jwtExpiration", 3600000L); // 1 hour

            String token = shortExpiryProvider.generateToken(authentication);

            assertThat(shortExpiryProvider.isTokenValid(token, userDetails)).isTrue();
        }
    }
}
