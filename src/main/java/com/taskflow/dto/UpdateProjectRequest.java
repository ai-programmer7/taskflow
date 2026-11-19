
package com.taskflow.dto;

import com.taskflow.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(

        @NotBlank @Size(max = 100) String name,

        @Size(max = 2000) String description,

        ProjectStatus status) {
}
