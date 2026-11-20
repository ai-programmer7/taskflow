package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.CreateProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.UpdateProjectRequest;
import com.taskflow.entity.ProjectStatus;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.exception.GlobalExceptionHandler;
import com.taskflow.exception.ProjectNotFoundException;
import com.taskflow.service.ProjectService;
import com.taskflow.service.TaskService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;

@WebMvcTest(ProjectController.class)
@Import({
                GlobalExceptionHandler.class,
                ProjectControllerTest.TestConfig.class
})
class ProjectControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private TaskService taskService;

        private final ObjectMapper objectMapper = JsonMapper.builder().build();

        @Autowired
        private ProjectService projectService;
        private final ProjectResponse projectResponse = new ProjectResponse(
                        1L,
                        "My First Project",
                        "My first project in TaskFlow",
                        ProjectStatus.COMPLETED,
                        2L,
                        "testuser",
                        LocalDateTime.parse("2026-11-19T19:15:06.491742"));

        @BeforeEach
        void setUp() {
                reset(projectService);
                reset(taskService);
        }

        @TestConfiguration
        static class TestConfig {

                @Bean
                ProjectService projectService() {
                        return mock(ProjectService.class);
                }

                @Bean
                TaskService taskService() {
                        return mock(TaskService.class);
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

        @Test
        void shouldUpdateProject() throws Exception {
                UpdateProjectRequest request = new UpdateProjectRequest(
                                null,
                                null,
                                ProjectStatus.COMPLETED);

                when(projectService.update(eq(1L), any(UpdateProjectRequest.class)))
                                .thenReturn(projectResponse);

                mockMvc.perform(patch("/api/projects/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        void shouldDeleteProject() throws Exception {
                doNothing().when(projectService).delete(1L);

                mockMvc.perform(delete("/api/projects/1"))
                                .andExpect(status().isNoContent());

                verify(projectService).delete(1L);
        }

        @Test
        void shouldReturnNotFoundWhenDeletingNonExistingProject() throws Exception {
                doThrow(new ProjectNotFoundException(999L))
                                .when(projectService).delete(999L);

                mockMvc.perform(delete("/api/projects/999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnTasksForProject() throws Exception {
                TaskResponse response = new TaskResponse(
                                1L,
                                "Test task",
                                "Task description",
                                TaskStatus.TODO,
                                TaskPriority.HIGH,
                                1L,
                                "TaskFlow",
                                null,
                                null,
                                null,
                                null);

                when(taskService.getByProjectId(1L))
                                .thenReturn(List.of(response));

                mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/1/tasks"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].title").value("Test task"));

                verify(taskService).getByProjectId(1L);
        }

}