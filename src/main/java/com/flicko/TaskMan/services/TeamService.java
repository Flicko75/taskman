package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.TeamUpdate;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        String normalizedName = team.getName().trim();

        if (teamRepository.existsByName(normalizedName)){
            throw new RuntimeException("Team name already exists");
        }

        team.setName(normalizedName);
        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, TeamUpdate team) {
        Team oldTeam = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        String normalizedName = team.getName().trim();
        if (!oldTeam.getName().equals(normalizedName)
                && teamRepository.existsByNameAndIdNot(normalizedName, oldTeam.getId())){
            throw new RuntimeException("Team name already exists");
        }

        oldTeam.setName(normalizedName);
        oldTeam.setDescription(team.getDescription());

        return teamRepository.save(oldTeam);
    }

    @Transactional
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getTasks().isEmpty())
            throw new RuntimeException("Can't remove while it has assigned tasks");

        team.getUsers().forEach(user -> user.setTeam(null));

        teamRepository.delete(team);
    }
}
