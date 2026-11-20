
package com.taskflow.dto;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Long projectId,
        String projectName,
        Long assigneeId,
        String assigneeUsername,
        LocalDate dueDate,
        LocalDateTime createdAt) {
}
