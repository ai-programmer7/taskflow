package com.taskflow;

import com.taskflow.dto.RegisterRequest;
import com.taskflow.dto.UserResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.UserAlreadyExistsException;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.taskflow.mapper.UserMapper;
import com.taskflow.entity.Role;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private UserMapper userMapper;

        @Mock
        private RoleRepository roleRepository;

        @InjectMocks
        private UserService userService;

        @Test
        void shouldRegisterUserSuccessfully() {
                RegisterRequest request = new RegisterRequest(
                                "testuser",
                                "test@example.com",
                                "StrongPass123");

                Role userRole = new Role();
                userRole.setId(1L);
                userRole.setName("ROLE_USER");

                when(roleRepository.findByName("ROLE_USER"))
                                .thenReturn(Optional.of(userRole));

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
                assertThat(result.getRoles())
                                .anySatisfy(role -> assertThat(role.getName()).isEqualTo("ROLE_USER"));

                verify(passwordEncoder).encode("StrongPass123");
                verify(userRepository).save(any(User.class));
        }

        @Test
        void shouldThrowExceptionWhenUsernameAlreadyExists() {
                RegisterRequest request = new RegisterRequest(
                                "testuser",
                                "new@example.com",
                                "StrongPass123");

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
                                "StrongPass123");

                when(userRepository.findByUsername("newuser"))
                                .thenReturn(Optional.empty());

                when(userRepository.findByEmail("test@example.com"))
                                .thenReturn(Optional.of(new User()));

                assertThatThrownBy(() -> userService.register(request))
                                .isInstanceOf(UserAlreadyExistsException.class)
                                .hasMessage("Email is already registered");

                verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void getById_shouldReturnUser_whenUserExists() {
                User user = new User();
                user.setId(1L);
                user.setUsername("testuser");
                user.setEmail("test@example.com");

                when(userRepository.findById(1L))
                                .thenReturn(Optional.of(user));

                User result = userService.getById(1L);

                assertThat(result).isEqualTo(user);
                verify(userRepository).findById(1L);
        }

        @Test
        void getById_shouldThrowException_whenUserDoesNotExist() {
                when(userRepository.findById(33L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.getById(33L))
                                .isInstanceOf(UserNotFoundException.class)
                                .hasMessage("User not found with id: 33");

                verify(userRepository).findById(33L);
        }

        @Test
        void shouldReturnAllUsers() {
                User firstUser = new User();
                firstUser.setId(1L);
                firstUser.setUsername("user1");
                firstUser.setEmail("user1@example.com");

                User secondUser = new User();
                secondUser.setId(2L);
                secondUser.setUsername("user2");
                secondUser.setEmail("user2@example.com");

                UserResponse firstResponse = new UserResponse(
                                1L, "user1", "user1@example.com");

                UserResponse secondResponse = new UserResponse(
                                2L, "user2", "user2@example.com");

                when(userRepository.findAll())
                                .thenReturn(List.of(firstUser, secondUser));

                when(userMapper.toResponse(firstUser)).thenReturn(firstResponse);
                when(userMapper.toResponse(secondUser)).thenReturn(secondResponse);

                List<UserResponse> result = userService.getAllUsers();

                assertThat(result).containsExactly(firstResponse, secondResponse);

                verify(userRepository).findAll();
                verify(userMapper).toResponse(firstUser);
                verify(userMapper).toResponse(secondUser);
        }

        @Test
        void shouldDeleteUser() {
                User user = new User();
                user.setId(1L);

                when(userRepository.findById(1L))
                                .thenReturn(Optional.of(user));

                userService.deleteUser(1L);

                verify(userRepository).delete(user);
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonExistingUser() {
                when(userRepository.findById(1L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.deleteUser(1L))
                                .isInstanceOf(UserNotFoundException.class);

                verify(userRepository, never()).delete(any(User.class));
        }
}
