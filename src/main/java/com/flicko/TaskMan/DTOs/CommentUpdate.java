package com.flicko.TaskMan.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentUpdate {

    @NotBlank
    private String content;

}
