package com.flicko.TaskMan;

import com.flicko.TaskMan.DTOs.UserResponse;
import com.flicko.TaskMan.DTOs.UserRoleUpdate;
import com.flicko.TaskMan.DTOs.UserUpdate;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.CommentRepository;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.services.UserService;
import com.flicko.TaskMan.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void deleteUser_throwsWhenUserNotFound(){
        Long userId = 1L;

        mockCurrentUser(UserRole.ADMIN, 99L);
        when(userRepository.findByIdAndDeletedFalse(userId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(userId));

        verify(taskRepository, never()).findByUserId(any());
        verify(commentRepository, never()).findByUserId(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_preventSelfDeletion(){
        Long userId = 1L;

        mockCurrentUser(UserRole.ADMIN, userId);

        User targetUser = createUser(userId, UserRole.ADMIN);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(targetUser));

        assertThrows(InvalidOperationException.class, () -> userService.deleteUser(userId));

        verify(taskRepository, never()).findByUserId(any());
        verify(commentRepository, never()).findByUserId(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_preventDeletingLastAdmin(){
        Long userId = 1L;

        mockCurrentUser(UserRole.ADMIN, 99L);

        User targetUser = createUser(userId, UserRole.ADMIN);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(targetUser));
        when(userRepository.countByRoleAndDeletedFalse(UserRole.ADMIN)).thenReturn(1L);

        assertThrows(InvalidOperationException.class, () -> userService.deleteUser(userId));

        verify(taskRepository, never()).findByUserId(any());
        verify(commentRepository, never()).findByUserId(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_softDeleteUserSuccessfully(){
        Long userId = 1L;

        mockCurrentUser(UserRole.ADMIN, 99L);

        User targetUser = createUser(userId, UserRole.MEMBER);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(targetUser));
        when(taskRepository.findByUserId(userId)).thenReturn(List.of());
        when(commentRepository.findByUserId(userId)).thenReturn(List.of());

        userService.deleteUser(userId);

        verify(taskRepository).findByUserId(userId);
        verify(taskRepository).saveAll(any());
        verify(commentRepository).findByUserId(userId);
        verify(commentRepository).deleteAll(any());
        verify(userRepository).save(targetUser);

        assertTrue(targetUser.isDeleted());
    }

    @Test
    void logoutCurrentUser_incrementsTokenVersion(){
        User currentUser = mockCurrentUser(UserRole.ADMIN, 1L);
        currentUser.setTokenVersion(0);

        userService.logoutCurrentUser();

        assertEquals(1,currentUser.getTokenVersion());
        verify(userRepository).save(currentUser);
    }

    @Test
    void forceLogoutUser_userNotFound(){
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN, 99L);
        User user = createUser(userId, UserRole.ADMIN);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.forceLogoutUser(userId));

        verify(userRepository, never()).save(any());
    }

    @Test
    void forceLogoutUser_preventSelfLogout(){
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN, userId);
        User user = createUser(userId, UserRole.ADMIN);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> userService.forceLogoutUser(userId));

        verify(userRepository, never()).save(user);
    }

    @Test
    void forceLogoutUser_success(){
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN, 99L);
        User user = createUser(userId, UserRole.ADMIN);
        user.setTokenVersion(0);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        userService.forceLogoutUser(userId);

        assertEquals(1, user.getTokenVersion());

        verify(userRepository).save(user);
    }

    @Test
    void addUser_adminCreatesUser() throws AccessDeniedException {
        mockCurrentUser(UserRole.ADMIN, 99L);
        User user = createUser(1L, UserRole.MEMBER);
        user.setPassword("abc");

        when(passwordEncoder.encode("abc")).thenReturn("XYZ");
        when(userRepository.save(user)).thenReturn(user);

        userService.addUser(user);

        assertEquals("XYZ", user.getPassword());

        verify(passwordEncoder).encode("abc");
        verify(userRepository).save(user);
    }

    @Test
    void addUser_notAdminAccess(){
        mockCurrentUser(UserRole.MEMBER, 99L);
        User user = createUser(1L, UserRole.MEMBER);
        user.setPassword("abc");

        assertThrows(AccessDeniedException.class, () -> userService.addUser(user));

        verify(passwordEncoder,never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_adminReturnsAllUsers() throws AccessDeniedException {
        Pageable pageable = PageRequest.of(0, 4);

        mockCurrentUser(UserRole.ADMIN, 1L);

        User user = createUser(2L, UserRole.MEMBER);

        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findByDeletedFalse(pageable)).thenReturn(page);

        userService.getAllUsers(pageable);

        verify(userRepository).findByDeletedFalse(pageable);
        verify(userRepository, never()).findByTeamIdAndDeletedFalse(any(), any());
    }

    @Test
    void getAllUsers_managerReturnsUsersInTeam() throws AccessDeniedException {
        Pageable pageable = PageRequest.of(0,4);

        User currentUser = mockCurrentUser(UserRole.MANAGER, 1L);
        Team team = new Team();
        team.setId(10L);
        currentUser.setTeam(team);

        User user = createUser(2L, UserRole.MEMBER);

        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findByTeamIdAndDeletedFalse(10L, pageable)).thenReturn(page);

        userService.getAllUsers(pageable);

        verify(userRepository, never()).findByDeletedFalse(any());
        verify(userRepository).findByTeamIdAndDeletedFalse(10L, pageable);
    }

    @Test
    void getAllUsers_managerWithoutTeam(){
        Pageable pageable = PageRequest.of(0,4);

        mockCurrentUser(UserRole.MANAGER, 1L);

        assertThrows(InvalidOperationException.class, () -> userService.getAllUsers(pageable));

        verify(userRepository, never()).findByDeletedFalse(any());
        verify(userRepository, never()).findByTeamIdAndDeletedFalse(any(), any());
    }

    @Test
    void getAllUsers_memberAccessDenied(){
        Pageable pageable = PageRequest.of(0,4);

        mockCurrentUser(UserRole.MEMBER, 1L);

        assertThrows(AccessDeniedException.class, () -> userService.getAllUsers(pageable));

        verify(userRepository, never()).findByDeletedFalse(any());
        verify(userRepository, never()).findByTeamIdAndDeletedFalse(any(), any());
    }

    @Test
    void getUserById_adminReturnsUser() throws AccessDeniedException {
        Long userId = 1L;

        mockCurrentUser(UserRole.ADMIN, 2L);
        User user = createUser(userId, UserRole.MEMBER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.getUserById(userId);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void getUserById_userNotFound(){
        Long userId = 1L;

        mockCurrentUser(UserRole.ADMIN, 2L);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void getUserById_managerSameTeam() throws AccessDeniedException {
        Long userId = 1L;

        User currentUser = mockCurrentUser(UserRole.MANAGER, 2L);
        Team team = new Team();
        team.setId(10L);
        currentUser.setTeam(team);
        User user = createUser(userId, UserRole.MEMBER);
        user.setTeam(team);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);

        verify(userRepository, never()).findById(any());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void getUserById_managerDifferentTeam() {
        Long userId = 1L;

        User currentUser = mockCurrentUser(UserRole.MANAGER, 2L);
        Team managerTeam = new Team();
        Team memberTeam = new Team();
        managerTeam.setId(9L);
        memberTeam.setId(10L);
        currentUser.setTeam(managerTeam);
        User user = createUser(userId, UserRole.MEMBER);
        user.setTeam(memberTeam);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> userService.getUserById(userId));

        verify(userRepository, never()).findById(any());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void getUserById_memberAccessDenied(){
        mockCurrentUser(UserRole.MEMBER, 1L);

        assertThrows(AccessDeniedException.class, () -> userService.getUserById(2L));

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void updateUser_userNotFound(){
        Long userId = 2L;
        mockCurrentUser(UserRole.ADMIN, 1L);

        UserUpdate update = new UserUpdate();
        update.setName("ABC");
        update.setEmail("abc@mail.com");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userId, update));

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_adminUpdatesUser() throws AccessDeniedException {
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN, 2L);

        User user = createUser(userId, UserRole.MEMBER);

        UserUpdate update = new UserUpdate();
        update.setName("ABC");
        update.setEmail("abc@mail.com");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(userId, update);

        assertEquals("ABC", user.getName());
        assertEquals("abc@mail.com", user.getEmail());

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_managerUpdatesSelf() throws AccessDeniedException {
        Long userId = 1L;
        mockCurrentUser(UserRole.MANAGER, userId);

        User user = createUser(userId, UserRole.MANAGER);

        UserUpdate update = new UserUpdate();
        update.setName("ABC");
        update.setEmail("abc@mail.com");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(userId, update);

        assertEquals("ABC", user.getName());
        assertEquals("abc@mail.com", user.getEmail());

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_managerUpdatesOtherUser() {
        Long userId = 1L;
        mockCurrentUser(UserRole.MANAGER, 2L);

        User user = createUser(userId, UserRole.MEMBER);

        UserUpdate update = new UserUpdate();
        update.setName("ABC");
        update.setEmail("abc@mail.com");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> userService.updateUser(userId, update));

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_memberUpdatesSelf() throws AccessDeniedException {
        Long userId = 1L;
        mockCurrentUser(UserRole.MEMBER, userId);

        User user = createUser(userId, UserRole.MEMBER);

        UserUpdate update = new UserUpdate();
        update.setName("ABC");
        update.setEmail("abc@mail.com");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(userId, update);

        assertEquals("ABC", user.getName());
        assertEquals("abc@mail.com", user.getEmail());

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_memberUpdatesOtherUser(){
        Long userId = 1L;
        mockCurrentUser(UserRole.MEMBER, 2L);

        User user = createUser(userId, UserRole.MANAGER);

        UserUpdate update = new UserUpdate();
        update.setName("ABC");
        update.setEmail("abc@mail.com");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> userService.updateUser(userId, update));

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_userNotFound(){
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN,2L);

        UserRoleUpdate update = new UserRoleUpdate();
        update.setRole(UserRole.MEMBER);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRole(userId, update));

        verify(userRepository, never()).countByRoleAndDeletedFalse(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_adminSelfDemotion(){
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN, userId);
        User user = createUser(userId, UserRole.ADMIN);

        UserRoleUpdate update = new UserRoleUpdate();
        update.setRole(UserRole.MEMBER);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> userService.updateUserRole(userId, update));

        verify(userRepository, never()).countByRoleAndDeletedFalse(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_lastAdminDemotion(){
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN, 2L);
        User user = createUser(userId, UserRole.ADMIN);

        UserRoleUpdate update = new UserRoleUpdate();
        update.setRole(UserRole.MEMBER);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.countByRoleAndDeletedFalse(UserRole.ADMIN)).thenReturn(1L);

        assertThrows(InvalidOperationException.class, () -> userService.updateUserRole(userId, update));

        verify(userRepository).countByRoleAndDeletedFalse(UserRole.ADMIN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_adminUpdatesUserRole() {
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN, 2L);
        User user = createUser(userId, UserRole.MEMBER);

        UserRoleUpdate update = new UserRoleUpdate();
        update.setRole(UserRole.MANAGER);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUserRole(userId, update);

        assertEquals(UserRole.MANAGER, user.getRole());

        verify(userRepository, never()).countByRoleAndDeletedFalse(any());
        verify(userRepository).save(user);
    }

    @Test
    void assignUser_userNotFound(){
        Long userId = 1L, teamId = 2L;
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.assignUser(userId, teamId));

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(teamRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignUser_assigningAdmin(){
        Long userId = 1L, teamId = 2L;
        User user = createUser(userId, UserRole.ADMIN);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> userService.assignUser(userId, teamId));

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(teamRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignUser_teamNotFound(){
        Long userId = 1L, teamId = 2L;
        User user = createUser(userId, UserRole.MEMBER);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.assignUser(userId, teamId));

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(teamRepository).findById(teamId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignUser_userAlreadyInSameTeam(){
        Long userId = 1L, teamId = 2L;
        User user = createUser(userId, UserRole.MEMBER);
        Team team = createTeam(teamId);
        user.setTeam(team);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        userService.assignUser(userId, teamId);

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(teamRepository).findById(teamId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignUser_userInDifferentTeam(){
        Long userId = 1L, teamId = 2L;
        User user = createUser(userId, UserRole.MEMBER);
        Team newTeam = createTeam(teamId);
        Team userTeam = createTeam(3L);
        user.setTeam(userTeam);
        user.setTasks(List.of());

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(newTeam));
        when(userRepository.save(user)).thenReturn(user);

        userService.assignUser(userId, teamId);

        assertEquals(teamId, user.getTeam().getId());

        verify(userRepository).findByIdAndDeletedFalse(userId);
        verify(teamRepository).findById(teamId);
        verify(userRepository).save(user);
    }

    @Test
    void unassignUser_userNotFound(){
        Long userId = 1L;
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.unassignUser(userId));

        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void unassignUser_userNotBelongToTeam(){
        Long userId = 1L;
        User user = createUser(userId, UserRole.MEMBER);
        user.setTeam(null);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> userService.unassignUser(userId));

        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void unassignUser_unassignmentSuccess(){
        Long userId = 1L, teamId = 2L;
        User user = createUser(userId, UserRole.MEMBER);
        Team team = createTeam(teamId);
        user.setTeam(team);
        user.setTasks(List.of());

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        userService.unassignUser(userId);

        assertNull(user.getTeam());

        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    private User mockCurrentUser(UserRole role, Long id){
        User user = new User();
        user.setId(id);
        user.setRole(role);

        when(securityUtils.getCurrentUser()).thenReturn(user);

        return user;
    }

    private User createUser(Long id, UserRole role){
        User user = new User();
        user.setId(id);
        user.setRole(role);

        return user;
    }

    private Team createTeam(Long id){
        Team team = new Team();
        team.setId(id);

        return team;
    }

}
