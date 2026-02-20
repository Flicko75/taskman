package com.flicko.TaskMan.DTOs;

import com.flicko.TaskMan.enums.TaskPriority;
import com.flicko.TaskMan.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUpdate {

    @NotBlank
    private String title;
    private String description;
    private TaskStatus status;
    @NotNull
    private TaskPriority priority;
    private LocalDateTime dueDate;

}
