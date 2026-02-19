package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.UserRoleUpdate;
import com.flicko.TaskMan.DTOs.UserUpdate;
import com.flicko.TaskMan.enums.UserRole;
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
public class UserService {

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1){
            throw new RuntimeException("Can't delete only ADMIN");
        }

        List<Task> tasks = taskRepository.findByUserId(id);

        tasks.forEach(t -> t.setUser(null));
        taskRepository.saveAll(tasks);

        userRepository.delete(user);

    }

    public User updateUser(Long id, UserUpdate user) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        oldUser.setName(user.getName());
        oldUser.setEmail(user.getEmail());
        oldUser.setPassword(user.getPassword());

        return userRepository.save(oldUser);
    }

    public User updateUserRole(Long id, UserRoleUpdate user) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        oldUser.setRole(user.getRole());

        return userRepository.save(oldUser);
    }
}
