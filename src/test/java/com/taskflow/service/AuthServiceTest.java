
package com.taskflow.service;

import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private AuthService authService;

        @Test
        void shouldLoginSuccessfully() {
                LoginRequest request = new LoginRequest(
                                "testuser",
                                "password123");

                User user = new User();
                user.setUsername("testuser");
                user.setPassword("encodedPassword");

                when(userRepository.findByUsername("testuser"))
                                .thenReturn(Optional.of(user));

                when(passwordEncoder.matches("password123", "encodedPassword"))
                                .thenReturn(true);

                when(jwtService.generateToken(user))
                                .thenReturn("test-jwt-token");
                AuthResponse response = authService.login(request);

                assertThat(response.token()).isEqualTo("test-jwt-token");

                verify(userRepository).findByUsername("testuser");
                verify(passwordEncoder).matches("password123", "encodedPassword");
                verify(jwtService).generateToken(user);
        }

        @Test
        void shouldThrowExceptionWhenUserDoesNotExist() {
                LoginRequest request = new LoginRequest(
                                "unknown",
                                "password123");

                when(userRepository.findByUsername("unknown"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> authService.login(request))
                                .isInstanceOf(BadCredentialsException.class);

                verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        void shouldThrowExceptionWhenPasswordIsIncorrect() {
                LoginRequest request = new LoginRequest(
                                "testuser",
                                "wrongPassword");

                User user = new User();
                user.setUsername("testuser");
                user.setPassword("encodedPassword");

                when(userRepository.findByUsername("testuser"))
                                .thenReturn(Optional.of(user));

                when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                                .thenReturn(false);

                assertThatThrownBy(() -> authService.login(request))
                                .isInstanceOf(BadCredentialsException.class);

                verify(jwtService, never()).generateToken(any(User.class));
        }
}
