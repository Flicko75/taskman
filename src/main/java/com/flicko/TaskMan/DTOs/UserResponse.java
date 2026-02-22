package com.flicko.TaskMan.DTOs;

import com.flicko.TaskMan.enums.UserRole;

public record UserResponse(
   Long id,
   String name,
   String email,
   UserRole role,
   Long teamId
) {}
