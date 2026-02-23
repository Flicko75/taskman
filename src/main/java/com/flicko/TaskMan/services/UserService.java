package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.*;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Comment;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.CommentRepository;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.utils.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final TeamRepository teamRepository;

    private final CommentRepository commentRepository;

    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);

        Page<UserResponse> mapped = users.map(this::mapToResponse);

        return PageMapper.toPageResponse(mapped);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToResponse(user);
    }

    public UserResponse addUser(User user) {
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1){
            throw new InvalidOperationException("Can't delete only ADMIN");
        }

        List<Task> tasks = taskRepository.findByUserId(id);
        tasks.forEach(t -> t.setUser(null));
        taskRepository.saveAll(tasks);

        List<Comment> comments = commentRepository.findByUserId(id);
        commentRepository.deleteAll(comments);

        userRepository.delete(user);

    }

    public UserResponse updateUser(Long id, UserUpdate user) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());

        return mapToResponse(userRepository.save(oldUser));
    }

    public UserResponse updateUserRole(Long id, UserRoleUpdate user) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        oldUser.setRole(user.getRole());

        return mapToResponse(userRepository.save(oldUser));
    }

    @Transactional
    public UserResponse assignUser(Long userId, Long teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (user.getTeam() != null &&
            user.getTeam().getId().equals(team.getId()))
            return mapToResponse(user);

        List<Task> tasks = user.getTasks();
        tasks.forEach(task -> task.setUser(null));

        user.setTeam(team);

        return mapToResponse(userRepository.save(user));

    }

    @Transactional
    public UserResponse unassignUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getTeam() == null){
            throw new InvalidOperationException("User is not assigned to any team");
        }

        user.getTasks().forEach(task -> task.setUser(null));

        user.setTeam(null);
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getTeam() != null ? user.getTeam().getId() : null
        );
    }
}
