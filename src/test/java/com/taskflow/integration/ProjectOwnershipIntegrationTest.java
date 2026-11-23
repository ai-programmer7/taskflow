
package com.taskflow.integration;

import com.taskflow.entity.Project;
import com.taskflow.entity.ProjectStatus;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.JwtService;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskStatus;
import com.taskflow.entity.TaskPriority;
import com.taskflow.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-for-taskflow-integration-tests-2026"
})
@Transactional
class ProjectOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void userShouldNotAccessAnotherUsersProject() throws Exception {

        // Arrange: get the existing USER role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        // Create project owner
        User owner = createUser("owner", "owner@example.com", userRole);

        // Create another user
        User anotherUser = createUser(
                "anotheruser",
                "anotheruser@example.com",
                userRole);

        // Create a project owned by the first user
        Project project = new Project();
        project.setName("Private project");
        project.setDescription("This project belongs to owner");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);

        project = projectRepository.save(project);

        // Generate JWT for the second user
        String token = jwtService.generateToken(anotherUser);

        // Act & Assert
        mockMvc.perform(get("/api/projects/{id}", project.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void userShouldNotUpdateAnotherUsersProject() throws Exception {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        User owner = createUser("owner2", "owner2@example.com", userRole);

        User anotherUser = createUser(
                "anotheruser2",
                "anotheruser2@example.com",
                userRole);

        Project project = new Project();
        project.setName("Private project");
        project.setDescription("Original description");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);

        project = projectRepository.save(project);

        String token = jwtService.generateToken(anotherUser);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/projects/{id}", project.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Hacked project"
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void userShouldNotDeleteAnotherUsersProject() throws Exception {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        User owner = createUser("owner3", "owner3@example.com", userRole);

        User anotherUser = createUser(
                "anotheruser3",
                "anotheruser3@example.com",
                userRole);

        Project project = new Project();
        project.setName("Private project");
        project.setDescription("This project must not be deleted");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);

        project = projectRepository.save(project);

        String token = jwtService.generateToken(anotherUser);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/projects/{id}", project.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // Verify that the project still exists
        org.assertj.core.api.Assertions.assertThat(
                projectRepository.findById(project.getId())).isPresent();
    }

    @Test
    void userShouldNotAccessAnotherUsersTask() throws Exception {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        User owner = createUser("taskowner", "taskowner@example.com", userRole);

        User anotherUser = createUser(
                "othertaskuser",
                "othertaskuser@example.com",
                userRole);

        // Create a project owned by the first user
        Project project = new Project();
        project.setName("Private task project");
        project.setDescription("Project with a private task");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);

        project = projectRepository.save(project);

        // Create a task inside that project
        Task task = new Task();
        task.setTitle("Private task");
        task.setDescription("This task belongs to another user");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setProject(project);

        task = taskRepository.save(task);

        // Generate JWT for the second user
        String token = jwtService.generateToken(anotherUser);

        mockMvc.perform(get("/api/tasks/{id}", task.getId())
                .header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void userShouldNotUpdateAnotherUsersTask() throws Exception {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        User owner = createUser("taskowner2", "taskowner2@example.com", userRole);

        User anotherUser = createUser(
                "othertaskuser2",
                "othertaskuser2@example.com",
                userRole);

        Project project = new Project();
        project.setName("Private task project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);

        project = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("Original task");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(project);

        task = taskRepository.save(task);

        String token = jwtService.generateToken(anotherUser);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/api/tasks/{id}", task.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Hacked task"
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void userShouldNotDeleteAnotherUsersTask() throws Exception {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        User owner = createUser("taskowner3", "taskowner3@example.com", userRole);

        User anotherUser = createUser(
                "othertaskuser3",
                "othertaskuser3@example.com",
                userRole);

        Project project = new Project();
        project.setName("Private task project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);

        project = projectRepository.save(project);

        Task task = new Task();
        task.setTitle("Task to protect");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setProject(project);

        task = taskRepository.save(task);

        String token = jwtService.generateToken(anotherUser);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/tasks/{id}", task.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        // Verify that the task still exists
        org.assertj.core.api.Assertions.assertThat(
                taskRepository.findById(task.getId())).isPresent();
    }

    @Test
    void userShouldNotCreateTaskInAnotherUsersProject() throws Exception {

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow();

        User owner = createUser("projectowner4", "projectowner4@example.com", userRole);

        User anotherUser = createUser(
                "taskcreator4",
                "taskcreator4@example.com",
                userRole);

        Project project = new Project();
        project.setName("Private project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOwner(owner);

        project = projectRepository.save(project);

        String token = jwtService.generateToken(anotherUser);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Unauthorized task",
                            "description": "Trying to create a task in someone else's project",
                            "status": "TODO",
                            "priority": "HIGH",
                            "projectId": %d
                        }
                        """.formatted(project.getId())))
                .andExpect(status().isNotFound());

        // Verify that no task was created
        org.assertj.core.api.Assertions.assertThat(
                taskRepository.findByProjectId(project.getId())).isEmpty();
    }

    private User createUser(String username, String email, Role role) {

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("test-password");
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }
}
