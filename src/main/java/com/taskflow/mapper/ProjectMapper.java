package com.taskflow.mapper;

import com.taskflow.dto.ProjectResponse;
import com.taskflow.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getOwner().getId(),
                project.getOwner().getUsername(),
                project.getCreatedAt());
    }
}