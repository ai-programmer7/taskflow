package com.taskflow.service;

import com.taskflow.dto.RegisterRequest;
import com.taskflow.entity.User;
import com.taskflow.exception.UserAlreadyExistsException;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "testuser",
                "test@example.com",
                "StrongPass123"
        );

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("StrongPass123"))
                .thenReturn("bcrypt-hash");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        User result = userService.register(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("bcrypt-hash");

        verify(passwordEncoder).encode("StrongPass123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "testuser",
                "new@example.com",
                "StrongPass123"
        );

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username is already taken");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "test@example.com",
                "StrongPass123"
        );

        when(userRepository.findByUsername("newuser"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }
}
