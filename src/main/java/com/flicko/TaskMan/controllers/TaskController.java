package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.PageResponse;
import com.flicko.TaskMan.DTOs.TaskResponse;
import com.flicko.TaskMan.DTOs.TaskUpdate;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public PageResponse<TaskResponse> getAllTasks(Pageable pageable) throws AccessDeniedException {
        return taskService.getAllTasks(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public TaskResponse getTask(@PathVariable Long id) throws AccessDeniedException {
        return taskService.getTaskById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TaskResponse createTask(@Valid @RequestBody Task task) throws AccessDeniedException {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdate task ) throws AccessDeniedException {
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void deleteTask(@PathVariable Long id) throws AccessDeniedException {
        taskService.deleteTask(id);
    }

    @PutMapping("/{taskId}/assign/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TaskResponse assignTask(@PathVariable Long taskId, @PathVariable Long userId) throws AccessDeniedException {
        return taskService.assignTask(taskId, userId);
    }

    @PutMapping("/{id}/unassign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public TaskResponse unassignTask(@PathVariable Long id) throws AccessDeniedException {
        return taskService.unassignTask(id);
    }

}
