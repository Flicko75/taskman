package com.flicko.TaskMan.DTOs;

import com.flicko.TaskMan.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdate {

    @NotNull
    private UserRole role;

}
