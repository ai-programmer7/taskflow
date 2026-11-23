package com.taskflow.controller;

import com.taskflow.dto.CreateProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.dto.TaskResponse;
import com.taskflow.dto.UpdateProjectRequest;
import com.taskflow.service.ProjectService;
import com.taskflow.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request) {
        return projectService.create(request);
    }

    @GetMapping
    public List<ProjectResponse> getAll() {
        return projectService.getAll();
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable Long id) {
        return projectService.getById(id);
    }

    @PatchMapping("/{id}")
    public ProjectResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }

    @GetMapping("/{projectId}/tasks")
    public List<TaskResponse> getProjectTasks(
            @PathVariable Long projectId) {
        return taskService.getByProjectId(projectId);
    }
}