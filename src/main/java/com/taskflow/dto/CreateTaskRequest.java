
package com.taskflow.dto;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(

        @NotBlank @Size(max = 150) String title,

        @Size(max = 2000) String description,

        @NotNull TaskStatus status,

        @NotNull TaskPriority priority,

        @NotNull Long projectId,

        Long assigneeId,

        LocalDate dueDate) {
}
