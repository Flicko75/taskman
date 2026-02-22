package com.flicko.TaskMan.DTOs;

import java.time.LocalDateTime;

public record TeamResponse(
   Long id,
   String name,
   String description,
   LocalDateTime createdAt
) {}
