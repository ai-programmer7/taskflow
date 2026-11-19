
package com.taskflow.dto;

import com.taskflow.entity.ProjectStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(

        @Pattern(regexp = ".*\\S.*", message = "Name must not be blank") @Size(max = 100) String name,

        @Size(max = 2000) String description,

        ProjectStatus status) {
}
