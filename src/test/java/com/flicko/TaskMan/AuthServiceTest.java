package com.flicko.TaskMan;

import com.flicko.TaskMan.DTOs.LoginRequest;
import com.flicko.TaskMan.DTOs.LoginResponse;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.security.jwt.JwtService;
import com.flicko.TaskMan.services.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginSuccessReturnsToken(){
        LoginRequest request = createRequest();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@mail.com",
                "password"
        );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        User user = new User();
        user.setEmail("test@mail.com");

        when(userRepository.findByEmailAndDeletedFalse("test@mail.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("12345678");

        LoginResponse response = authService.login(request);

        verify(userRepository)
                .findByEmailAndDeletedFalse("test@mail.com");
        verify(jwtService)
                .generateToken(user);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        assertNotNull(savedUser.getLastLoginAt());

        assertEquals("12345678", response.message());
    }

    @Test
    void loginThrowsExceptionWhenUserNotFound(){
        LoginRequest request = createRequest();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "test@mail.com",
                "password"
        );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(userRepository.findByEmailAndDeletedFalse("test@mail.com"))
                .thenReturn(Optional.empty());
        verify(userRepository, never())
                .save(any());
        verify(jwtService, never())
                .generateToken(any());

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void loginThrowsExceptionWhenAuthenticationFails(){
        LoginRequest request = createRequest();

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Wrong credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(userRepository, never())
                .findByEmailAndDeletedFalse(anyString());
        verify(userRepository, never())
                .save(any());
        verify(jwtService, never())
                .generateToken(any());
    }

    private LoginRequest createRequest(){
        LoginRequest request = new LoginRequest();
        request.setEmail("test@mail.com");
        request.setPassword("password");
        return request;
    }

}
