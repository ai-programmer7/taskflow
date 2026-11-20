
package com.taskflow.dto;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(

        @Pattern(regexp = ".*\\S.*", message = "Title must not be blank") @Size(max = 150) String title,

        @Size(max = 2000) String description,

        TaskStatus status,

        TaskPriority priority,

        Long assigneeId,

        LocalDate dueDate) {
}
