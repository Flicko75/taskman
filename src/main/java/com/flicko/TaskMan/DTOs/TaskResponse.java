package com.flicko.TaskMan.DTOs;

import com.flicko.TaskMan.enums.TaskPriority;
import com.flicko.TaskMan.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime createdAt,
        LocalDateTime dueDate,
        Long userId,
        Long teamId
) {}
