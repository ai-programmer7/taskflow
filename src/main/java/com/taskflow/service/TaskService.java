
package com.taskflow.service;

import com.taskflow.dto.CreateTaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.UpdateTaskRequest;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.ProjectNotFoundException;
import com.taskflow.exception.TaskNotFoundException;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        String username = getCurrentUsername();

        Project project = projectRepository
                .findByIdAndOwnerUsername(request.projectId(), username)
                .orElseThrow(() -> new ProjectNotFoundException(request.projectId()));

        User assignee = null;

        if (request.assigneeId() != null) {
            assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new UserNotFoundException(request.assigneeId()));
        }

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setProject(project);
        task.setAssignee(assignee);
        task.setDueDate(request.dueDate());

        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAll() {
        String username = getCurrentUsername();

        return taskRepository.findByProjectOwnerUsername(username)
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getByProjectId(Long projectId) {
        String username = getCurrentUsername();

        Project project = projectRepository
                .findByIdAndOwnerUsername(projectId, username)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        return taskRepository.findByProjectId(project.getId())
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        String username = getCurrentUsername();

        Task task = taskRepository
                .findByIdAndProjectOwnerUsername(id, username)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        String username = getCurrentUsername();

        Task task = taskRepository
                .findByIdAndProjectOwnerUsername(id, username)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.title() != null) {
            task.setTitle(request.title());
        }

        if (request.description() != null) {
            task.setDescription(request.description());
        }

        if (request.status() != null) {
            task.setStatus(request.status());
        }

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new UserNotFoundException(request.assigneeId()));

            task.setAssignee(assignee);
        }

        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }

        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    public void delete(Long id) {
        String username = getCurrentUsername();

        Task task = taskRepository
                .findByIdAndProjectOwnerUsername(id, username)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskRepository.delete(task);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }
}
