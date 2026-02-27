package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.PageResponse;
import com.flicko.TaskMan.DTOs.UserResponse;
import com.flicko.TaskMan.DTOs.UserRoleUpdate;
import com.flicko.TaskMan.DTOs.UserUpdate;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) throws AccessDeniedException {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public UserResponse getUser(@PathVariable Long id) throws AccessDeniedException {
        return userService.getUserById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse addUser(@Valid @RequestBody User user) throws AccessDeniedException {
        return userService.addUser(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdate user) throws AccessDeniedException {
        return userService.updateUser(id, user);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserRole(@PathVariable Long id, @Valid @RequestBody UserRoleUpdate user){
        return userService.updateUserRole(id, user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

    @PutMapping("/{userId}/assign/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse assignUser(@PathVariable Long userId, @PathVariable Long teamId){
        return userService.assignUser(userId, teamId);
    }

    @PutMapping("/{userId}/unassign")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse unassignUser(@PathVariable Long userId){
        return userService.unassignUser(userId);
    }

}
