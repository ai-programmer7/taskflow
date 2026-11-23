
package com.taskflow.service;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.UpdateTaskRequest;
import com.taskflow.entity.*;
import com.taskflow.exception.ProjectNotFoundException;
import com.taskflow.exception.TaskNotFoundException;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

        @Mock
        private TaskRepository taskRepository;

        @Mock
        private ProjectRepository projectRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private TaskMapper taskMapper;

        @InjectMocks
        private TaskService taskService;

        @BeforeEach
        void setUp() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                                "testuser",
                                                null,
                                                List.of()));
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        @Test
        void shouldCreateTask() {
                Project project = new Project();
                project.setId(1L);
                project.setName("TaskFlow");

                User assignee = new User();
                assignee.setId(2L);
                assignee.setUsername("testuser");

                CreateTaskRequest request = new CreateTaskRequest(
                                "Implement authentication",
                                "Add login and registration",
                                TaskStatus.IN_PROGRESS,
                                TaskPriority.HIGH,
                                1L,
                                2L,
                                LocalDate.of(2026, 12, 1));

                when(projectRepository.findByIdAndOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(project));

                when(userRepository.findById(2L))
                                .thenReturn(Optional.of(assignee));

                when(taskRepository.save(any(Task.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                TaskResponse expectedResponse = new TaskResponse(
                                1L,
                                "Implement authentication",
                                "Add login and registration",
                                TaskStatus.IN_PROGRESS,
                                TaskPriority.HIGH,
                                1L,
                                "TaskFlow",
                                2L,
                                "testuser",
                                LocalDate.of(2026, 12, 1),
                                null);

                when(taskMapper.toResponse(any(Task.class)))
                                .thenReturn(expectedResponse);

                TaskResponse result = taskService.create(request);

                assertThat(result).isEqualTo(expectedResponse);

                verify(taskRepository).save(any(Task.class));
                verify(taskMapper).toResponse(any(Task.class));
        }

        @Test
        void shouldThrowExceptionWhenProjectNotFound() {
                CreateTaskRequest request = new CreateTaskRequest(
                                "Test task",
                                null,
                                TaskStatus.TODO,
                                TaskPriority.MEDIUM,
                                999L,
                                null,
                                null);

                when(projectRepository.findByIdAndOwnerUsername(999L, "testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.create(request))
                                .isInstanceOf(ProjectNotFoundException.class);

                verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        void shouldThrowExceptionWhenAssigneeNotFound() {
                Project project = new Project();
                project.setId(1L);

                CreateTaskRequest request = new CreateTaskRequest(
                                "Test task",
                                null,
                                TaskStatus.TODO,
                                TaskPriority.MEDIUM,
                                1L,
                                999L,
                                null);

                when(projectRepository.findByIdAndOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(project));

                when(userRepository.findById(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.create(request))
                                .isInstanceOf(UserNotFoundException.class);

                verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        void shouldReturnAllTasks() {
                Task task = new Task();

                when(taskRepository.findByProjectOwnerUsername("testuser"))
                                .thenReturn(List.of(task));

                TaskResponse response = mock(TaskResponse.class);
                when(taskMapper.toResponse(task)).thenReturn(response);

                List<TaskResponse> result = taskService.getAll();

                assertThat(result).hasSize(1);
                assertThat(result.getFirst()).isEqualTo(response);
        }

        @Test
        void shouldReturnTaskById() {
                Task task = new Task();
                task.setId(1L);

                TaskResponse response = mock(TaskResponse.class);

                when(taskRepository.findByIdAndProjectOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(task));

                when(taskMapper.toResponse(task)).thenReturn(response);

                TaskResponse result = taskService.getById(1L);

                assertThat(result).isEqualTo(response);
        }

        @Test
        void shouldThrowExceptionWhenTaskNotFound() {
                when(taskRepository.findByIdAndProjectOwnerUsername(999L, "testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.getById(999L))
                                .isInstanceOf(TaskNotFoundException.class);
        }

        @Test
        void shouldReturnTasksByProjectId() {
                Project project = new Project();
                project.setId(1L);

                Task task = new Task();
                task.setId(1L);

                TaskResponse response = mock(TaskResponse.class);

                when(projectRepository.findByIdAndOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(project));

                when(taskRepository.findByProjectId(1L))
                                .thenReturn(List.of(task));

                when(taskMapper.toResponse(task)).thenReturn(response);

                List<TaskResponse> result = taskService.getByProjectId(1L);

                assertThat(result).hasSize(1);
                assertThat(result.getFirst()).isEqualTo(response);
        }

        @Test
        void shouldThrowExceptionWhenProjectForTasksNotFound() {
                when(projectRepository.findByIdAndOwnerUsername(999L, "testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.getByProjectId(999L))
                                .isInstanceOf(ProjectNotFoundException.class);
        }

        @Test
        void shouldUpdateTask() {
                Task task = new Task();
                task.setId(1L);
                task.setTitle("Old title");
                task.setStatus(TaskStatus.TODO);
                task.setPriority(TaskPriority.LOW);

                Project project = new Project();
                project.setId(1L);
                project.setName("TaskFlow");
                task.setProject(project);

                UpdateTaskRequest request = new UpdateTaskRequest(
                                "New title",
                                "Updated description",
                                TaskStatus.IN_PROGRESS,
                                TaskPriority.HIGH,
                                null,
                                null);

                when(taskRepository.findByIdAndProjectOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(task));

                when(taskRepository.save(task)).thenReturn(task);

                TaskResponse response = mock(TaskResponse.class);
                when(taskMapper.toResponse(task)).thenReturn(response);

                TaskResponse result = taskService.update(1L, request);

                assertThat(result).isEqualTo(response);
                assertThat(task.getTitle()).isEqualTo("New title");
                assertThat(task.getDescription()).isEqualTo("Updated description");
                assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
                assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);

                verify(taskRepository).save(task);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonExistingTask() {
                UpdateTaskRequest request = new UpdateTaskRequest(
                                "New title",
                                null,
                                null,
                                null,
                                null,
                                null);

                when(taskRepository.findByIdAndProjectOwnerUsername(999L, "testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.update(999L, request))
                                .isInstanceOf(TaskNotFoundException.class);

                verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        void shouldDeleteTask() {
                Task task = new Task();
                task.setId(1L);

                when(taskRepository.findByIdAndProjectOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(task));

                taskService.delete(1L);

                verify(taskRepository).delete(task);
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonExistingTask() {
                when(taskRepository.findByIdAndProjectOwnerUsername(999L, "testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> taskService.delete(999L))
                                .isInstanceOf(TaskNotFoundException.class);

                verify(taskRepository, never()).delete(any(Task.class));
        }
}
