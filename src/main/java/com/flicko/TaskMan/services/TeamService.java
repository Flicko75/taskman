package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.TeamResponse;
import com.flicko.TaskMan.DTOs.TeamUpdate;
import com.flicko.TaskMan.exceptions.DuplicateResourceException;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.repos.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    public List<TeamResponse> getAllTeams() {
        List<Team> teams = teamRepository.findAll();

        return teams.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        return mapToResponse(team);
    }

    public TeamResponse createTeam(Team team) {
        String normalizedName = team.getName().trim();

        if (teamRepository.existsByName(normalizedName)){
            throw new DuplicateResourceException("Team name already exists");
        }

        team.setName(normalizedName);
        return mapToResponse(teamRepository.save(team));
    }

    public TeamResponse updateTeam(Long id, TeamUpdate team) {
        Team oldTeam = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        String normalizedName = team.getName().trim();
        if (!oldTeam.getName().equals(normalizedName)
                && teamRepository.existsByNameAndIdNot(normalizedName, oldTeam.getId())){
            throw new DuplicateResourceException("Team name already exists");
        }

        oldTeam.setName(normalizedName);
        oldTeam.setDescription(team.getDescription());

        return mapToResponse(teamRepository.save(oldTeam));
    }

    @Transactional
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (!team.getTasks().isEmpty())
            throw new InvalidOperationException("Can't remove while it has assigned tasks");

        team.getUsers().forEach(user -> user.setTeam(null));

        teamRepository.delete(team);
    }

    private TeamResponse mapToResponse(Team team){
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt()
        );
    }
}
