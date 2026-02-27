package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.TeamResponse;
import com.flicko.TaskMan.DTOs.TeamUpdate;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.DuplicateResourceException;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    private final SecurityUtils securityUtils;

    public List<TeamResponse> getAllTeams() throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();
        Team team = user.getTeam();

        List<Team> teams;

        if (user.getRole() == UserRole.ADMIN){
            teams = teamRepository.findAll();
        } else if (user.getRole() == UserRole.MANAGER || user.getRole() == UserRole.MEMBER) {
            if (team != null)
                teams = List.of(team);
            else
                teams = List.of();
        } else
            throw new AccessDeniedException("Accesss Denied");

        return teams.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TeamResponse getTeamById(Long id) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (user.getRole() == UserRole.ADMIN){

        } else if (user.getRole() == UserRole.MANAGER || user.getRole() == UserRole.MEMBER) {
            if (user.getTeam() == null ||
                !user.getTeam().getId().equals(team.getId()))
                throw new AccessDeniedException("Can't access other team info");

        } else
            throw new AccessDeniedException("Accesss Denied");

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
