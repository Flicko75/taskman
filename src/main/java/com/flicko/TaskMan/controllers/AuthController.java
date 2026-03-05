package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.LoginRequest;
import com.flicko.TaskMan.DTOs.LoginResponse;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.security.jwt.JwtService;
import com.flicko.TaskMan.security.userdetails.CustomUserDetails;
import com.flicko.TaskMan.services.AuthService;
import com.flicko.TaskMan.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public void logout(){
        userService.logoutCurrentUser();
    }

}
