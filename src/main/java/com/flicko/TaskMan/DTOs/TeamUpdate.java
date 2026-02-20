package com.flicko.TaskMan.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamUpdate {

    @NotBlank
    private String name;
    private String description;

}
