package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.TaskUpdate;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<Task> getAllTasks(){
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id){
        return taskService.getTaskById(id);
    }

    @PostMapping
    public Task createTask(@RequestBody Task task){
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody TaskUpdate task ){
        return taskService.updateTask(id, task);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
    }

    @PutMapping("/{taskId}/assign/{userId}")
    public Task assignTask(@PathVariable Long taskId, @PathVariable Long userId){
        return taskService.assignTask(taskId, userId);
    }

}
