package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.LoginRequest;
import com.flicko.TaskMan.DTOs.LoginResponse;
import com.flicko.TaskMan.security.jwt.JwtService;
import com.flicko.TaskMan.security.userdetails.CustomUserDetails;
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

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String email = authentication.getName();
        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        String token = jwtService.generateToken(email, role);

        return ResponseEntity.ok(new LoginResponse(token));
    }

}
