package com.flicko.TaskMan.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreate {

    @NotBlank
    private String content;
    @NotBlank
    private Long userId;

}
