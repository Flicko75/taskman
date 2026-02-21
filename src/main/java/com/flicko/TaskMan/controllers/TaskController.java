package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.TaskResponse;
import com.flicko.TaskMan.DTOs.TaskUpdate;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<TaskResponse> getAllTasks(){
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id){
        return taskService.getTaskById(id);
    }

    @PostMapping
    public TaskResponse createTask(@Valid @RequestBody Task task){
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdate task ){
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }

    @PutMapping("/{taskId}/assign/{userId}")
    public TaskResponse assignTask(@PathVariable Long taskId, @PathVariable Long userId){
        return taskService.assignTask(taskId, userId);
    }

    @PutMapping("/{id}/unassign")
    public TaskResponse unassignTask(@PathVariable Long id){
        return taskService.unassignTask(id);
    }

}
