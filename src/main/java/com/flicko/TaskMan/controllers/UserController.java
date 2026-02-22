package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.UserResponse;
import com.flicko.TaskMan.DTOs.UserRoleUpdate;
import com.flicko.TaskMan.DTOs.UserUpdate;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id){
        return userService.getUserById(id);
    }

    @PostMapping
    public UserResponse addUser(@Valid @RequestBody User user){
        return userService.addUser(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdate user){
        return userService.updateUser(id, user);
    }

    @PutMapping("/{id}/role")
    public UserResponse updateUserRole(@PathVariable Long id, @Valid @RequestBody UserRoleUpdate user){
        return userService.updateUserRole(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

    @PutMapping("/{userId}/assign/{teamId}")
    public UserResponse assignUser(@PathVariable Long userId, @PathVariable Long teamId){
        return userService.assignUser(userId, teamId);
    }

    @PutMapping("/{userId}/unassign")
    public UserResponse unassignUser(@PathVariable Long userId){
        return userService.unassignUser(userId);
    }

}
