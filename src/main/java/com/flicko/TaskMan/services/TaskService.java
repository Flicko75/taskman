package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.PageResponse;
import com.flicko.TaskMan.DTOs.TaskResponse;
import com.flicko.TaskMan.DTOs.TaskUpdate;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.utils.PageMapper;
import com.flicko.TaskMan.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TeamRepository teamRepository;

    private final SecurityUtils securityUtils;

    public PageResponse<TaskResponse> getAllTasks(Pageable pageable) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        Page<Task> page;

        if (user.getRole() == UserRole.ADMIN){
            page = taskRepository.findAll(pageable);
        }
        else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null)
                throw new InvalidOperationException("Team not assigned");
            page = taskRepository.findByTeamId(user.getTeam().getId(), pageable);
        }
        else if (user.getRole() == UserRole.MEMBER) {
            page = taskRepository.findByUserId(user.getId(), pageable);
        }
        else {
            throw new AccessDeniedException("Access Denied");
        }

        Page<TaskResponse> mapped = page.map(this::mapToResponse);

        return PageMapper.toPageResponse(mapped);
    }

    public TaskResponse getTaskById(Long id) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Task Found"));

        if (user.getRole() == UserRole.ADMIN){
        }
        else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null ||
                    !task.getTeam().getId().equals(user.getTeam().getId()))
                throw new AccessDeniedException("Team id is wrong");
        }
        else if (user.getRole() == UserRole.MEMBER) {
            if (task.getUser() == null ||
                !task.getUser().getId().equals(user.getId()))
                throw new AccessDeniedException("Task not assigned to current user");
        }
        else {
            throw new AccessDeniedException("Access Denied");
        }

        return mapToResponse(task);
    }

    public TaskResponse createTask(Task task) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        if (user.getRole() == UserRole.MEMBER)
            throw new AccessDeniedException("Access Denied");

        if (task.getTeam() == null)
            throw new InvalidOperationException("Task not assigned");

        Team team = teamRepository.findById(task.getTeam().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (user.getRole() == UserRole.ADMIN){
            task.setTeam(team);
        } else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null ||
                !team.getId().equals(user.getTeam().getId()))
                throw new InvalidOperationException("Team id is wrong");
            task.setTeam(team);
        } else
            throw new AccessDeniedException("Access Denied");

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskUpdate task) throws AccessDeniedException {
        Task oldTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No Task Found"));

        User user = securityUtils.getCurrentUser();

        if (user.getRole() == UserRole.MEMBER) {
            if (oldTask.getUser() == null ||
                !oldTask.getUser().getEmail().equals(user.getEmail())){
                throw new AccessDeniedException("You can only update your own assigned task");
            }
        }
        if (user.getRole() == UserRole.MANAGER){
            if (user.getTeam() == null ||
                oldTask.getTeam() == null ||
                !oldTask.getTeam().getId().equals(user.getTeam().getId())){
                throw new AccessDeniedException("You can only update tasks within your own team");
            }
        }

        oldTask.setTitle(task.getTitle());
        oldTask.setDescription(task.getDescription());
        oldTask.setStatus(task.getStatus());
        oldTask.setPriority(task.getPriority());
        oldTask.setDueDate(task.getDueDate());

        return mapToResponse(taskRepository.save(oldTask));
    }

    public void deleteTask(Long id) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        if (user.getRole() == UserRole.MEMBER)
            throw new AccessDeniedException("Access Denied");

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (user.getRole() == UserRole.ADMIN){

        } else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null ||
                    !task.getTeam().getId().equals(user.getTeam().getId()))
                throw new AccessDeniedException("Can't delete task of other teams");
        } else
            throw new AccessDeniedException("Access Denied");

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse assignTask(Long taskId, Long userId) throws AccessDeniedException {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getRole() == UserRole.MEMBER)
            throw new AccessDeniedException("Access Denied");

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

        if (currentUser.getRole() == UserRole.ADMIN){
            task.setUser(user);
        } else if (currentUser.getRole() == UserRole.MANAGER) {
            if (currentUser.getTeam() == null ||
                !currentUser.getTeam().getId().equals(user.getTeam().getId()))
                throw new InvalidOperationException("Can't assign task to other team");
            task.setUser(user);
        } else
            throw new AccessDeniedException("Access Denied");

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse unassignTask(Long id) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRole.MEMBER)
            throw new AccessDeniedException("Access Denied");

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getUser() == null) {
            throw new InvalidOperationException("Task is already unassigned");
        }

        if (user.getRole() == UserRole.ADMIN){
            task.setUser(null);
        } else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null ||
                    !task.getTeam().getId().equals(user.getTeam().getId()))
                throw new AccessDeniedException("Can't unassign task outside of your team");
            task.setUser(null);
        } else
            throw new AccessDeniedException("Access Denied");

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
