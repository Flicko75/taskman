package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.TaskUpdate;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.repos.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No Task Found"));
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, TaskUpdate task) {
        Task oldTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No Task Found"));

        oldTask.setTitle(task.getTitle());
        oldTask.setDescription(task.getDescription());
        oldTask.setStatus(task.getStatus());
        oldTask.setPriority(task.getPriority());
        oldTask.setDueDate(task.getDueDate());

        return taskRepository.save(oldTask);
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No Task Found"));

        taskRepository.delete(task);
    }
}
