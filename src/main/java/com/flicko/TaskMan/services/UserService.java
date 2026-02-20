package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.UserRoleUpdate;
import com.flicko.TaskMan.DTOs.UserUpdate;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final TeamRepository teamRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User addUser(User user) {
        return userRepository.save(user);
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

        userRepository.delete(user);

    }

    public User updateUser(Long id, UserUpdate user) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());

        return userRepository.save(oldUser);
    }

    public User updateUserRole(Long id, UserRoleUpdate user) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        oldUser.setRole(user.getRole());

        return userRepository.save(oldUser);
    }

    @Transactional
    public User assignUser(Long userId, Long teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (user.getTeam() != null &&
            user.getTeam().getId().equals(team.getId()))
            return user;

        List<Task> tasks = user.getTasks();
        tasks.forEach(task -> task.setUser(null));

        user.setTeam(team);

        return userRepository.save(user);

    }

    @Transactional
    public User unassignUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getTeam() == null){
            throw new InvalidOperationException("User is not assigned to any team");
        }

        user.getTasks().forEach(task -> task.setUser(null));

        user.setTeam(null);
        return user;
    }
}
