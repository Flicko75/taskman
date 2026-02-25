package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.TeamResponse;
import com.flicko.TaskMan.DTOs.TeamUpdate;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.services.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public List<TeamResponse> getAllTeam(){
        return teamService.getAllTeams();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public TeamResponse getTeam(@PathVariable Long id){
        return teamService.getTeamById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse createTeam(@Valid @RequestBody Team team){
        return teamService.createTeam(team);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse updateTeam(@PathVariable Long id, @Valid @RequestBody TeamUpdate team){
        return teamService.updateTeam(id, team);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTeam(@PathVariable Long id){
        teamService.deleteTeam(id);
    }

}
