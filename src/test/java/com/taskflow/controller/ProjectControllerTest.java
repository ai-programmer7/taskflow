package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.CreateProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.entity.ProjectStatus;
import com.taskflow.exception.GlobalExceptionHandler;
import com.taskflow.exception.ProjectNotFoundException;
import com.taskflow.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.json.JsonMapper;

@WebMvcTest(ProjectController.class)
@Import({
        GlobalExceptionHandler.class,
        ProjectControllerTest.TestConfig.class
})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Autowired
    private ProjectService projectService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        ProjectService projectService() {
            return mock(ProjectService.class);
        }
    }

    @Test
    void shouldCreateProject() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest(
                "TaskFlow",
                "Task management project",
                ProjectStatus.ACTIVE,
                1L);

        ProjectResponse response = new ProjectResponse(
                1L,
                "TaskFlow",
                "Task management project",
                ProjectStatus.ACTIVE,
                1L,
                "testuser",
                null);

        when(projectService.create(any(CreateProjectRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("TaskFlow"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        String invalidRequest = """
                {
                    "name": "",
                    "description": "Description",
                    "status": "ACTIVE",
                    "ownerId": 1
                }
                """;

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    void shouldReturnAllProjects() throws Exception {
        ProjectResponse response = new ProjectResponse(
                1L,
                "TaskFlow",
                "Task management project",
                ProjectStatus.ACTIVE,
                1L,
                "testuser",
                null);

        when(projectService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("TaskFlow"));
    }

    @Test
    void shouldReturnProjectById() throws Exception {
        ProjectResponse response = new ProjectResponse(
                1L,
                "TaskFlow",
                "Task management project",
                ProjectStatus.ACTIVE,
                1L,
                "testuser",
                null);

        when(projectService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("TaskFlow"));
    }

    @Test
    void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
        when(projectService.getById(99L))
                .thenThrow(new ProjectNotFoundException(99L));

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound());
    }
}