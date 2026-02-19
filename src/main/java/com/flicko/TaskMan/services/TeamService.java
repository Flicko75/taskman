package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.TeamUpdate;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.repos.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    public Team createTeam(Team team) {
        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, TeamUpdate team) {
        Team oldTeam = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        oldTeam.setName(team.getName());
        oldTeam.setDescription(team.getDescription());

        return teamRepository.save(oldTeam);
    }

    public void deleteTeam(Long id) {
        Team oldTeam = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        teamRepository.delete(oldTeam);
    }
}
