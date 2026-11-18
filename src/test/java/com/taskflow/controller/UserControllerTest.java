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
                        "Username is already taken"
                ));

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
}
