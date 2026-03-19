package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.LoginRequest;
import com.flicko.TaskMan.DTOs.LoginResponse;
import com.flicko.TaskMan.services.AuthService;
import com.flicko.TaskMan.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(new LoginResponse("Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public void logout(){
        userService.logoutCurrentUser();
    }

}
