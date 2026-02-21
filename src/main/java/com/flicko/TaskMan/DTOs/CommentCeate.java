package com.flicko.TaskMan.DTOs;

import jakarta.validation.constraints.NotBlank;

public class CommentCeate {

    @NotBlank
    private String content;
    @NotBlank
    private Long userId;

}
