package com.flicko.TaskMan.DTOs;

import com.flicko.TaskMan.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdate {

    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;

}
