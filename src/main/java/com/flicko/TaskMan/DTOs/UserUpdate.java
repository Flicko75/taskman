package com.flicko.TaskMan.DTOs;

import com.flicko.TaskMan.enums.UserRole;
import lombok.Data;

@Data
public class UserUpdate {

    private String name;
    private String email;
    private String password;

}
