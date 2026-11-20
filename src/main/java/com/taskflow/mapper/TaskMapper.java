
package com.taskflow.mapper;

import com.taskflow.dto.TaskResponse;
import com.taskflow.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignee() != null
                        ? task.getAssignee().getId()
                        : null,
                task.getAssignee() != null
                        ? task.getAssignee().getUsername()
                        : null,
                task.getDueDate(),
                task.getCreatedAt());
    }
}
