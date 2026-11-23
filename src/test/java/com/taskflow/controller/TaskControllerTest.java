
package com.taskflow.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.UpdateTaskRequest;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.exception.GlobalExceptionHandler;
import com.taskflow.exception.TaskNotFoundException;
import com.taskflow.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
                GlobalExceptionHandler.class,
                TaskControllerTest.TestConfig.class,
                ValidationAutoConfiguration.class
})
class TaskControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private TaskService taskService;

        private final JsonMapper objectMapper = JsonMapper.builder()
                        .findAndAddModules()
                        .build();

        @BeforeEach
        void setUp() {
                reset(taskService);
        }

        @TestConfiguration
        static class TestConfig {

                @Bean
                TaskService taskService() {
                        return mock(TaskService.class);
                }
        }

        @Test
        void shouldCreateTask() throws Exception {
                CreateTaskRequest request = new CreateTaskRequest(
                                "Test task",
                                "Test description",
                                TaskStatus.TODO,
                                TaskPriority.HIGH,
                                1L,
                                null,
                                LocalDate.of(2026, 12, 1));

                TaskResponse response = new TaskResponse(
                                1L,
                                "Test task",
                                "Test description",
                                TaskStatus.TODO,
                                TaskPriority.HIGH,
                                1L,
                                "TaskFlow",
                                null,
                                null,
                                LocalDate.of(2026, 12, 1),
                                null);

                when(taskService.create(any(CreateTaskRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("Test task"))
                                .andExpect(jsonPath("$.priority").value("HIGH"));

                verify(taskService).create(any(CreateTaskRequest.class));
        }

        @Test
        void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
                String invalidRequest = """
                                {
                                    "title": "",
                                    "status": "TODO",
                                    "priority": "HIGH",
                                    "projectId": 1
                                }
                                """;

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest))
                                .andExpect(status().isBadRequest());

                verify(taskService, never()).create(any(CreateTaskRequest.class));
        }

        @Test
        void shouldReturnAllTasks() throws Exception {
                TaskResponse response = new TaskResponse(
                                1L,
                                "Test task",
                                "Description",
                                TaskStatus.TODO,
                                TaskPriority.HIGH,
                                1L,
                                "TaskFlow",
                                null,
                                null,
                                null,
                                null);

                when(taskService.getAll()).thenReturn(List.of(response));

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/tasks"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].title").value("Test task"));

                verify(taskService).getAll();
        }

        @Test
        void shouldReturnTaskById() throws Exception {
                TaskResponse response = new TaskResponse(
                                1L,
                                "Test task",
                                "Description",
                                TaskStatus.TODO,
                                TaskPriority.HIGH,
                                1L,
                                "TaskFlow",
                                null,
                                null,
                                null,
                                null);

                when(taskService.getById(1L)).thenReturn(response);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/tasks/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("Test task"));

                verify(taskService).getById(1L);
        }

        @Test
        void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
                when(taskService.getById(999L))
                                .thenThrow(new TaskNotFoundException(999L));

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/tasks/999"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldUpdateTask() throws Exception {
                UpdateTaskRequest request = new UpdateTaskRequest(
                                "Updated task",
                                "Updated description",
                                TaskStatus.IN_PROGRESS,
                                TaskPriority.HIGH,
                                null,
                                null);

                TaskResponse response = new TaskResponse(
                                1L,
                                "Updated task",
                                "Updated description",
                                TaskStatus.IN_PROGRESS,
                                TaskPriority.HIGH,
                                1L,
                                "TaskFlow",
                                null,
                                null,
                                null,
                                null);

                when(taskService.update(eq(1L), any(UpdateTaskRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .patch("/api/tasks/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated task"))
                                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

                verify(taskService).update(eq(1L), any(UpdateTaskRequest.class));
        }

        @Test
        void shouldReturnNotFoundWhenUpdatingNonExistingTask() throws Exception {
                UpdateTaskRequest request = new UpdateTaskRequest(
                                "Updated task",
                                null,
                                null,
                                null,
                                null,
                                null);

                when(taskService.update(eq(999L), any(UpdateTaskRequest.class)))
                                .thenThrow(new TaskNotFoundException(999L));

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .patch("/api/tasks/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldDeleteTask() throws Exception {
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/tasks/1"))
                                .andExpect(status().isNoContent());

                verify(taskService).delete(1L);
        }

        @Test
        void shouldReturnNotFoundWhenDeletingNonExistingTask() throws Exception {
                doThrow(new TaskNotFoundException(999L))
                                .when(taskService).delete(999L);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/tasks/999"))
                                .andExpect(status().isNotFound());

                verify(taskService).delete(999L);
        }
}
