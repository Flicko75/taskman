package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.TeamResponse;
import com.flicko.TaskMan.DTOs.TeamUpdate;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.services.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public List<TeamResponse> getAllTeam(){
        return teamService.getAllTeams();
    }

    @GetMapping("/{id}")
    public TeamResponse getTeam(@PathVariable Long id){
        return teamService.getTeamById(id);
    }

    @PostMapping
    public TeamResponse createTeam(@Valid @RequestBody Team team){
        return teamService.createTeam(team);
    }

    @PutMapping("/{id}")
    public TeamResponse updateTeam(@PathVariable Long id, @Valid @RequestBody TeamUpdate team){
        return teamService.updateTeam(id, team);
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id){
        teamService.deleteTeam(id);
    }

}
