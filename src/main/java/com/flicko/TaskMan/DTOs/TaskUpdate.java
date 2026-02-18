package com.flicko.TaskMan.DTOs;

import com.flicko.TaskMan.enums.TaskPriority;
import com.flicko.TaskMan.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskUpdate {

    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;

}
