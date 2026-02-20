package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.TaskUpdate;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

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

    @Transactional
    public Task assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (task.getTeam() == null || user.getTeam() == null){
            throw new RuntimeException("Task and User need to belong to a team");
        }

        if (!task.getTeam().getId().equals(user.getTeam().getId())){
            throw new RuntimeException("User needs to be of same team as task");
        }
        task.setUser(user);

        return taskRepository.save(task);
    }

    public Task unassignTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getUser() == null) {
            throw new RuntimeException("Task is already unassigned");
        }
        task.setUser(null);

        return taskRepository.save(task);
    }
}
