
package com.taskflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.taskflow.service.UserService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-for-taskflow-security-tests-2026"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void healthEndpointShouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointShouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersEndpointShouldReturn403ForUserRole() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(jwt().authorities(() -> "ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersEndpointShouldReturn200ForAdminRole() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users")
                .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }
}
