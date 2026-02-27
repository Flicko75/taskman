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
import com.flicko.TaskMan.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final TeamRepository teamRepository;

    private final CommentRepository commentRepository;

    private final PasswordEncoder passwordEncoder;
    
    private final SecurityUtils securityUtils;

    public PageResponse<UserResponse> getAllUsers(Pageable pageable) throws AccessDeniedException {
        User currentUser = securityUtils.getCurrentUser();

        Page<User> users;

        if (currentUser.getRole() == UserRole.ADMIN){
            users = userRepository.findAll(pageable);
        } else if (currentUser.getRole() == UserRole.MANAGER) {
            if (currentUser.getTeam() == null)
                throw new InvalidOperationException("User not assigned to any team");
            users = userRepository.findByTeamId(currentUser.getTeam().getId(), pageable);
        } else
            throw new AccessDeniedException("Access Denied");

        Page<UserResponse> mapped = users.map(this::mapToResponse);

        return PageMapper.toPageResponse(mapped);
    }

    public UserResponse getUserById(Long id) throws AccessDeniedException {
        User currentUser = securityUtils.getCurrentUser();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (currentUser.getRole() == UserRole.ADMIN){
            
        } else if (currentUser.getRole() == UserRole.MANAGER) {
            if (currentUser.getTeam() == null ||
                user.getTeam() == null ||
                !currentUser.getTeam().getId().equals(user.getTeam().getId()))
                throw new AccessDeniedException("User not in same team as logged user");
        } else
            throw new AccessDeniedException("Access Denied");

        return mapToResponse(user);
    }

    public UserResponse addUser(User user) throws AccessDeniedException {
        User currentUser = securityUtils.getCurrentUser();

        if (!(currentUser.getRole() == UserRole.ADMIN)){
            throw new AccessDeniedException("Access Denied");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

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

    public UserResponse updateUser(Long id, UserUpdate user) throws AccessDeniedException {
        User currentUser = securityUtils.getCurrentUser();

        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() == UserRole.ADMIN){

        } else if (currentUser.getRole() == UserRole.MANAGER || currentUser.getRole() == UserRole.MEMBER) {
            if (!currentUser.getId().equals(oldUser.getId()))
                throw new AccessDeniedException("Can't update other users");
        } else
            throw new AccessDeniedException("Access Denied");

        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());

        return mapToResponse(userRepository.save(oldUser));
    }

    public UserResponse updateUserRole(Long id, UserRoleUpdate user) {
        User currentUser = securityUtils.getCurrentUser();

        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getId().equals(oldUser.getId()))
            throw new InvalidOperationException("Self demotion not allowed");
        if (oldUser.getRole() == UserRole.ADMIN &&
            user.getRole() != UserRole.ADMIN &&
            userRepository.countByRole(UserRole.ADMIN) <= 1)
            throw new InvalidOperationException("Can't remove last admin");
        oldUser.setRole(user.getRole());

        return mapToResponse(userRepository.save(oldUser));
    }

    @Transactional
    public UserResponse assignUser(Long userId, Long teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == UserRole.ADMIN){
            throw new InvalidOperationException("Can't assign admin to any team");
        }

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
