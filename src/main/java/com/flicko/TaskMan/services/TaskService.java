package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.PageResponse;
import com.flicko.TaskMan.DTOs.TaskResponse;
import com.flicko.TaskMan.DTOs.TaskUpdate;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.utils.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    public PageResponse<TaskResponse> getAllTasks(Pageable pageable) {
        Page<Task> page = taskRepository.findAll(pageable);

        Page<TaskResponse> mapped = page.map(this::mapToResponse);

        return PageMapper.toPageResponse(mapped);
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No Task Found"));

        return mapToResponse(task);
    }

    public TaskResponse createTask(Task task) {
        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskUpdate task) {
        Task oldTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No Task Found"));

        oldTask.setTitle(task.getTitle());
        oldTask.setDescription(task.getDescription());
        oldTask.setStatus(task.getStatus());
        oldTask.setPriority(task.getPriority());
        oldTask.setDueDate(task.getDueDate());

        return mapToResponse(taskRepository.save(oldTask));
    }

    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (task.getTeam() == null || user.getTeam() == null){
            throw new InvalidOperationException("Task and User need to belong to a team");
        }

        if (!task.getTeam().getId().equals(user.getTeam().getId())){
            throw new InvalidOperationException("User needs to be of same team as task");
        }
        task.setUser(user);

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse unassignTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getUser() == null) {
            throw new InvalidOperationException("Task is already unassigned");
        }
        task.setUser(null);

        return mapToResponse(taskRepository.save(task));
    }

    private TaskResponse mapToResponse(Task task){
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getDueDate(),
                task.getUser() != null ? task.getUser().getId() : null,
                task.getTeam() != null ? task.getTeam().getId() : null
        );
    }
}
