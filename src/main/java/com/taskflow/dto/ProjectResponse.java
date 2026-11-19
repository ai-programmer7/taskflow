package com.taskflow.dto;

import com.taskflow.entity.ProjectStatus;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        ProjectStatus status,
        Long ownerId,
        String ownerUsername,
        LocalDateTime createdAt) {
}