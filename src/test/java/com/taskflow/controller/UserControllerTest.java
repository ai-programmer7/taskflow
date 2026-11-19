package com.taskflow.controller;

import com.taskflow.dto.RegisterRequest;
import com.taskflow.entity.User;
import com.taskflow.exception.GlobalExceptionHandler;
import com.taskflow.exception.UserAlreadyExistsException;
import com.taskflow.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.taskflow.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;

import com.taskflow.exception.UserNotFoundException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.List;
import com.taskflow.dto.UserResponse;

@WebMvcTest(UserController.class)
@Import({
                GlobalExceptionHandler.class,
                UserControllerTest.TestConfig.class,
                ValidationAutoConfiguration.class
})
class UserControllerTest {
        @BeforeEach
        void resetMocks() {
                reset(userService);
        }

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserService userService;

        @TestConfiguration
        static class TestConfig {
                @Bean
                UserService userService() {
                        return mock(UserService.class);
                }

                @Bean
                UserMapper userMapper() {
                        return new UserMapper();
                }
        }

        @Test
        void shouldRegisterUserSuccessfully() throws Exception {
                User user = new User();
                user.setId(1L);
                user.setUsername("testuser");
                user.setEmail("test@example.com");

                when(userService.register(any(RegisterRequest.class)))
                                .thenReturn(user);

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "username": "testuser",
                                                    "email": "test@example.com",
                                                    "password": "StrongPass123"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.password").doesNotExist());
                verify(userService).register(any(RegisterRequest.class));
        }

        @Test
        void shouldReturnBadRequestForInvalidData() throws Exception {
                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "username": "",
                                                    "email": "wrong-email",
                                                    "password": "123"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.details.username").exists())
                                .andExpect(jsonPath("$.details.email").exists())
                                .andExpect(jsonPath("$.details.password").exists());

                verifyNoInteractions(userService);
        }

        @Test
        void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
                when(userService.register(any(RegisterRequest.class)))
                                .thenThrow(new UserAlreadyExistsException(
                                                "Username is already taken"));

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "username": "testuser",
                                                    "email": "test@example.com",
                                                    "password": "StrongPass123"
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").value("Conflict"))
                                .andExpect(jsonPath("$.message")
                                                .value("Username is already taken"));
        }

        @Test
        void shouldReturnUserSuccessfully() throws Exception {
                User user = new User();
                user.setId(1L);
                user.setUsername("testuser");
                user.setEmail("test@example.com");

                when(userService.getById(1L)).thenReturn(user);

                mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.password").doesNotExist());

                verify(userService).getById(1L);
        }

        @Test
        void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
                when(userService.getById(33L))
                                .thenThrow(new UserNotFoundException(
                                                "User not found with id: 33"));

                mockMvc.perform(get("/api/users/33"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error")
                                                .value("User not found with id: 33"));

                verify(userService).getById(33L);
        }

        @Test
        void shouldReturnAllUsers() throws Exception {
                List<UserResponse> users = List.of(
                                new UserResponse(1L, "user1", "user1@example.com"),
                                new UserResponse(2L, "user2", "user2@example.com"));

                when(userService.getAllUsers()).thenReturn(users);

                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].username").value("user1"))
                                .andExpect(jsonPath("$[1].username").value("user2"))
                                .andExpect(jsonPath("$[0].password").doesNotExist());

                verify(userService).getAllUsers();
        }
}
