package com.flicko.TaskMan;

import com.flicko.TaskMan.DTOs.TeamResponse;
import com.flicko.TaskMan.DTOs.TeamUpdate;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.DuplicateResourceException;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.services.TeamService;
import com.flicko.TaskMan.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TeamService teamService;

    @Test
    void getAllTeams_adminReturnsAllTeams() throws AccessDeniedException {
        Long userId = 1L, teamId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);
        Team team = createTeam(teamId);

        when(teamRepository.findAll()).thenReturn(List.of(team));

        List<TeamResponse> teams = teamService.getAllTeams();

        assertEquals(1, teams.size());
        assertEquals(teamId, teams.getFirst().id());

        verify(teamRepository).findAll();
    }

    @Test
    void getAllTeams_managerWithTeam() throws AccessDeniedException {
        Long userId = 1L, teamId = 2L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Team team = createTeam(teamId);
        user.setTeam(team);

        List<TeamResponse> teams = teamService.getAllTeams();

        assertEquals(1, teams.size());
        assertEquals(teamId, teams.getFirst().id());

        verify(teamRepository, never()).findAll();
    }

    @Test
    void getAllTeams_managerWithoutTeam() throws AccessDeniedException {
        Long userId = 1L;
        mockCurrentUser(UserRole.MANAGER, userId);

        List<TeamResponse> teams = teamService.getAllTeams();

        assertTrue(teams.isEmpty());

        verify(teamRepository, never()).findAll();
    }

    @Test
    void getAllTeams_memberWithTeam() throws AccessDeniedException {
        Long userId = 1L, teamId = 2L;
        User user = mockCurrentUser(UserRole.MEMBER, userId);
        Team team = createTeam(teamId);
        user.setTeam(team);

        List<TeamResponse> teams = teamService.getAllTeams();

        assertEquals(1, teams.size());
        assertEquals(teamId, teams.getFirst().id());

        verify(teamRepository, never()).findAll();
    }

    @Test
    void getAllTeams_memberWithoutTeam() throws AccessDeniedException {
        Long userId = 1L;
        mockCurrentUser(UserRole.MEMBER, userId);

        List<TeamResponse> teams = teamService.getAllTeams();

        assertTrue(teams.isEmpty());

        verify(teamRepository, never()).findAll();
    }

    @Test
    void getTeamById_teamNotFound(){
        Long userId = 1L, teamId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamById(teamId));

        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_adminAccess() throws AccessDeniedException {
        Long userId = 1L, teamId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);
        Team team = createTeam(teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        TeamResponse response = teamService.getTeamById(teamId);

        assertEquals(teamId, response.id());

        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_managerSameTeam() throws AccessDeniedException {
        Long userId = 1L, teamId = 2L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Team team = createTeam(teamId);
        user.setTeam(team);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        TeamResponse response = teamService.getTeamById(teamId);

        assertEquals(teamId, response.id());

        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_managerDifferentTeam(){
        Long userId = 1L, teamId = 2L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Team requestTeam = createTeam(teamId);

        Team userTeam = createTeam(3L);
        user.setTeam(userTeam);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(requestTeam));

        assertThrows(AccessDeniedException.class, () -> teamService.getTeamById(teamId));

        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_managerWithoutTeam(){
        Long userId = 1L, teamId = 2L;
        mockCurrentUser(UserRole.MANAGER, userId);
        Team team = createTeam(teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        assertThrows(AccessDeniedException.class, () -> teamService.getTeamById(teamId));

        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_memberSameTeam() throws AccessDeniedException {
        Long userId = 1L, teamId = 2L;
        User user = mockCurrentUser(UserRole.MEMBER, userId);
        Team team = createTeam(teamId);
        user.setTeam(team);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        TeamResponse response = teamService.getTeamById(teamId);

        assertEquals(teamId, response.id());

        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_memberDifferentTeam(){
        Long userId = 1L, teamId = 2L;
        User user = mockCurrentUser(UserRole.MEMBER, userId);
        Team requestTeam = createTeam(teamId);

        Team userTeam = createTeam(3L);
        user.setTeam(userTeam);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(requestTeam));

        assertThrows(AccessDeniedException.class, () -> teamService.getTeamById(teamId));

        verify(teamRepository).findById(teamId);
    }

    @Test
    void getTeamById_memberWithoutTeam(){
        Long userId = 1L, teamId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);
        Team team = createTeam(teamId);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        assertThrows(AccessDeniedException.class, () -> teamService.getTeamById(teamId));

        verify(teamRepository).findById(teamId);
    }

    @Test
    void createTeam_success(){
        Long teamId = 1L;
        Team team = createTeam(teamId);
        team.setName("  ABC  ");

        when(teamRepository.existsByName("ABC")).thenReturn(false);
        when(teamRepository.save(team)).thenReturn(team);

        teamService.createTeam(team);

        verify(teamRepository).existsByName("ABC");

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());
        Team response = teamCaptor.getValue();
        assertEquals("ABC", response.getName());
    }

    @Test
    void createTeam_duplicateTeamName(){
        Long teamId = 1L;
        Team team = createTeam(teamId);
        team.setName("  ABC  ");

        when(teamRepository.existsByName("ABC")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teamService.createTeam(team));

        verify(teamRepository).existsByName("ABC");
        verify(teamRepository, never()).save(any());
    }

    @Test
    void updateTeam_teamNotFound(){
        Long teamId = 1L;
        TeamUpdate team = new TeamUpdate();
        team.setName("ABC");

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.updateTeam(teamId, team));

        verify(teamRepository).findById(teamId);
        verify(teamRepository, never()).existsByNameAndIdNot(any(), any());
        verify(teamRepository, never()).save(any());
    }

    @Test
    void updateTeam_success(){
        Long teamId = 1L;
        Team team = createTeam(teamId);
        team.setName("ABC");
        TeamUpdate teamUpdate = new TeamUpdate();
        teamUpdate.setName("  DEF  ");
        teamUpdate.setDescription("def");

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.existsByNameAndIdNot("DEF", teamId)).thenReturn(false);
        when(teamRepository.save(team)).thenReturn(team);

        teamService.updateTeam(teamId, teamUpdate);

        verify(teamRepository).existsByNameAndIdNot("DEF", teamId);

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());
        Team response = teamCaptor.getValue();

        assertEquals("DEF", response.getName());
        assertEquals("def", response.getDescription());
    }

    @Test
    void updateTeam_duplicateName(){
        Long teamId = 1L;
        Team team = createTeam(teamId);
        team.setName("ABC");
        TeamUpdate teamUpdate = new TeamUpdate();
        teamUpdate.setName("  DEF  ");
        teamUpdate.setDescription("def");

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.existsByNameAndIdNot("DEF", teamId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teamService.updateTeam(teamId, teamUpdate));

        verify(teamRepository).existsByNameAndIdNot("DEF", teamId);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void updateTeam_sameNameNoDuplicateCheck(){
        Long teamId = 1L;
        Team team = createTeam(teamId);
        team.setName("ABC");
        TeamUpdate teamUpdate = new TeamUpdate();
        teamUpdate.setName("  ABC  ");
        teamUpdate.setDescription("def");

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(teamRepository.save(team)).thenReturn(team);

        teamService.updateTeam(teamId, teamUpdate);

        verify(teamRepository, never()).existsByNameAndIdNot(any(), any());

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(teamCaptor.capture());
        Team response = teamCaptor.getValue();

        assertEquals("ABC", response.getName());
        assertEquals("def", response.getDescription());
    }

    @Test
    void deleteTeam_teamNotFound(){
        Long teamId = 1L;

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.deleteTeam(teamId));

        verify(teamRepository).findById(teamId);
        verify(teamRepository, never()).delete(any());
    }

    @Test
    void deleteTeam_teamHasTasks(){
        Long teamId = 1L;

        Task task = new Task();

        Team team = createTeam(teamId);
        team.setTasks(List.of(task));

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        assertThrows(InvalidOperationException.class, () -> teamService.deleteTeam(teamId));

        verify(teamRepository).findById(teamId);
        verify(teamRepository, never()).delete(any());
    }

    @Test
    void deleteTeam_success(){
        Long teamId = 1L;

        Team team = createTeam(teamId);
        User user1 = new User();
        User user2 = new User();

        user1.setTeam(team);
        user2.setTeam(team);

        team.setUsers(List.of(user1, user2));
        team.setTasks(List.of());

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        teamService.deleteTeam(teamId);

        verify(teamRepository).findById(teamId);
        verify(teamRepository).delete(team);

        assertNull(user1.getTeam());
        assertNull(user2.getTeam());
    }

    private User mockCurrentUser(UserRole role, Long id){
        User user = new User();
        user.setId(id);
        user.setRole(role);

        when(securityUtils.getCurrentUser()).thenReturn(user);

        return user;
    }

    private Team createTeam(Long id){
        Team team = new Team();
        team.setId(id);

        return team;
    }

}
