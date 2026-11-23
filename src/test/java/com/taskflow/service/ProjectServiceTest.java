package com.taskflow.service;

import com.taskflow.dto.CreateProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.dto.UpdateProjectRequest;
import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectStatus;
import com.taskflow.entity.User;
import com.taskflow.exception.ProjectNotFoundException;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.mapper.ProjectMapper;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

        @Mock
        private ProjectRepository projectRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ProjectMapper projectMapper;

        @InjectMocks
        private ProjectService projectService;

        private User owner;
        private Project project;
        private ProjectResponse projectResponse;

        @BeforeEach
        void setUp() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                                "testuser",
                                                null,
                                                List.of()));
                owner = new User();
                owner.setId(1L);
                owner.setUsername("testuser");

                project = new Project();
                project.setId(1L);
                project.setName("TaskFlow");
                project.setDescription("Task management project");
                project.setStatus(ProjectStatus.ACTIVE);
                project.setOwner(owner);

                projectResponse = new ProjectResponse(
                                1L,
                                "TaskFlow",
                                "Task management project",
                                ProjectStatus.ACTIVE,
                                1L,
                                "testuser",
                                null);
        }

        @Test
        void shouldCreateProject() {
                CreateProjectRequest request = new CreateProjectRequest(
                                "TaskFlow",
                                "Task management project",
                                ProjectStatus.ACTIVE);

                when(userRepository.findByUsername("testuser"))
                                .thenReturn(Optional.of(owner));

                when(projectRepository.save(any(Project.class)))
                                .thenReturn(project);

                when(projectMapper.toResponse(project))
                                .thenReturn(projectResponse);

                ProjectResponse result = projectService.create(request);

                assertThat(result).isEqualTo(projectResponse);

                verify(userRepository).findByUsername("testuser");
                verify(projectRepository).save(any(Project.class));
                verify(projectMapper).toResponse(project);
        }

        @Test
        void shouldThrowExceptionWhenOwnerNotFound() {
                CreateProjectRequest request = new CreateProjectRequest(
                                "TaskFlow",
                                "Description",
                                ProjectStatus.ACTIVE);

                when(userRepository.findByUsername("testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> projectService.create(request))
                                .isInstanceOf(UserNotFoundException.class);

                verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        void shouldReturnAllProjects() {
                when(projectRepository.findByOwnerUsername("testuser"))
                                .thenReturn(List.of(project));

                when(projectMapper.toResponse(project))
                                .thenReturn(projectResponse);

                List<ProjectResponse> result = projectService.getAll();

                assertThat(result)
                                .hasSize(1)
                                .containsExactly(projectResponse);

                verify(projectRepository).findByOwnerUsername("testuser");
                verify(projectMapper).toResponse(project);
        }

        @Test
        void shouldReturnProjectById() {
                when(projectRepository.findByIdAndOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(project));

                when(projectMapper.toResponse(project))
                                .thenReturn(projectResponse);

                ProjectResponse result = projectService.getById(1L);

                assertThat(result).isEqualTo(projectResponse);

                verify(projectRepository).findByIdAndOwnerUsername(1L, "testuser");
                verify(projectMapper).toResponse(project);
        }

        @Test
        void shouldThrowExceptionWhenProjectNotFound() {
                when(projectRepository.findByIdAndOwnerUsername(99L, "testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> projectService.getById(99L))
                                .isInstanceOf(ProjectNotFoundException.class);

                verify(projectMapper, never()).toResponse(any(Project.class));
        }

        @Test
        void shouldUpdateProject() {
                UpdateProjectRequest request = new UpdateProjectRequest(
                                null,
                                null,
                                ProjectStatus.COMPLETED);

                when(projectRepository.findByIdAndOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(project));

                when(projectRepository.save(project))
                                .thenReturn(project);

                when(projectMapper.toResponse(project))
                                .thenReturn(projectResponse);

                ProjectResponse result = projectService.update(1L, request);

                assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
                assertThat(result).isEqualTo(projectResponse);

                verify(projectRepository).findByIdAndOwnerUsername(1L, "testuser");
                verify(projectRepository).save(project);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonExistingProject() {
                UpdateProjectRequest request = new UpdateProjectRequest(
                                "Updated name",
                                null,
                                null);

                when(projectRepository.findByIdAndOwnerUsername(999L, "testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> projectService.update(999L, request))
                                .isInstanceOf(ProjectNotFoundException.class);

                verify(projectRepository, never()).save(any(Project.class));
        }

        @Test
        void shouldDeleteProject() {
                when(projectRepository.findByIdAndOwnerUsername(1L, "testuser"))
                                .thenReturn(Optional.of(project));

                projectService.delete(1L);

                verify(projectRepository).findByIdAndOwnerUsername(1L, "testuser");
                verify(projectRepository).delete(project);
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonExistingProject() {
                when(projectRepository.findByIdAndOwnerUsername(999L, "testuser"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> projectService.delete(999L))
                                .isInstanceOf(ProjectNotFoundException.class);

                verify(projectRepository, never()).delete(any(Project.class));
        }

        @org.junit.jupiter.api.AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }
}