package com.flicko.TaskMan.DTOs;

import java.time.LocalDateTime;

public record CommentResponse(
   Long id,
   String content,
   LocalDateTime createdAt,
   Long taskId,
   Long userId
) {}
