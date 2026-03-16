package com.flicko.TaskMan;

import com.flicko.TaskMan.DTOs.PageResponse;
import com.flicko.TaskMan.DTOs.TaskResponse;
import com.flicko.TaskMan.DTOs.TaskUpdate;
import com.flicko.TaskMan.enums.TaskPriority;
import com.flicko.TaskMan.enums.TaskStatus;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.TeamRepository;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.services.TaskService;
import com.flicko.TaskMan.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.swing.text.html.Option;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_adminReturnsAll() throws AccessDeniedException {
        Long userId = 1L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task1 = new Task();
        Task task2 = new Task();

        Page<Task> page = new PageImpl<>(List.of(task1, task2));

        when(taskRepository.findAll(pageable)).thenReturn(page);

        PageResponse<TaskResponse> result = taskService.getAllTasks(pageable);

        assertEquals(2, result.content().size());

        verify(taskRepository).findAll(pageable);
        verify(taskRepository, never()).findByTeamId(any(), any());
        verify(taskRepository, never()).findByUserId(any(), any());
    }

    @Test
    void getAllTasks_managerReturnsTeamTasks() throws AccessDeniedException {
        Long userId = 1L, teamId = 2L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Team team = createTeam(teamId);
        user.setTeam(team);

        Pageable pageable = PageRequest.of(0, 4);

        Task task1 = new Task();
        Task task2 = new Task();

        Page<Task> page = new PageImpl<>(List.of(task1, task2));

        when(taskRepository.findByTeamId(teamId, pageable)).thenReturn(page);

        PageResponse<TaskResponse> result = taskService.getAllTasks(pageable);

        assertEquals(2, result.content().size());

        verify(taskRepository, never()).findAll((Pageable) any());
        verify(taskRepository).findByTeamId(teamId, pageable);
        verify(taskRepository, never()).findByUserId(any(), any());
    }

    @Test
    void getAllTasks_managerWithoutTeam(){
        Long userId = 1L;
        mockCurrentUser(UserRole.MANAGER, userId);

        Pageable pageable = PageRequest.of(0, 4);

        assertThrows(InvalidOperationException.class, () -> taskService.getAllTasks(pageable));

        verify(taskRepository, never()).findAll((Pageable) any());
        verify(taskRepository, never()).findByTeamId(any(), any());
        verify(taskRepository, never()).findByUserId(any(), any());
    }

    @Test
    void getAllTasks_memberReturnsOwnTasks() throws AccessDeniedException {
        Long userId = 1L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task1 = new Task();
        Task task2 = new Task();

        Page<Task> page = new PageImpl<>(List.of(task1, task2));

        when(taskRepository.findByUserId(userId, pageable)).thenReturn(page);

        PageResponse<TaskResponse> result = taskService.getAllTasks(pageable);

        assertEquals(2, result.content().size());

        verify(taskRepository, never()).findAll((Pageable) any());
        verify(taskRepository, never()).findByTeamId(any(), any());
        verify(taskRepository).findByUserId(userId, pageable);
    }

    @Test
    void getTaskById_taskNotFound(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(taskId));

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_adminAccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);
        Task task = createTask(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.getTaskById(taskId);

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_managerSameTeam() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        user.setTeam(team);
        task.setTeam(team);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.getTaskById(taskId);

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_managerDifferentTeam(){
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Task task = createTask(taskId);
        Team taskTeam = createTeam(teamId);
        Team userTeam = createTeam(4L);

        user.setTeam(userTeam);
        task.setTeam(taskTeam);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(taskId));

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_managerWithoutTeam(){
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        user.setTeam(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(taskId));

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_memberAssignedTask() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L;
        User user = mockCurrentUser(UserRole.MEMBER, userId);
        Task task = createTask(taskId);

        task.setUser(user);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.getTaskById(taskId);

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_memberDifferentTask(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);
        Task task = createTask(taskId);

        User assignedUser = createUser(3L);
        task.setUser(assignedUser);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(taskId));

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_memberTaskWithoutUser(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);
        Task task = createTask(taskId);

        task.setUser(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(taskId));

        verify(taskRepository).findById(taskId);
    }

    @Test
    void createTask_memberAccessDenied(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);
        Task task = createTask(taskId);

        assertThrows(AccessDeniedException.class, () -> taskService.createTask(task));

        verify(teamRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_taskWithoutTeam(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);
        Task task = createTask(taskId);
        task.setTeam(null);

        assertThrows(InvalidOperationException.class, () -> taskService.createTask(task));

        verify(teamRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_teamNotFound(){
        Long userId = 1L, taskId = 2L, teamId = 3L;
        mockCurrentUser(UserRole.ADMIN, userId);
        Task task = createTask(taskId);
        Team team = createTeam(teamId);
        task.setTeam(team);

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(task));

        verify(teamRepository).findById(teamId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, teamId = 3L;
        mockCurrentUser(UserRole.ADMIN, userId);
        Task task = createTask(taskId);
        Team team = createTeam(teamId);
        task.setTeam(team);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.createTask(task);

        verify(teamRepository).findById(teamId);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task result = taskCaptor.getValue();

        assertEquals(team, result.getTeam());
    }

    @Test
    void createTask_managerSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        user.setTeam(team);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.createTask(task);

        verify(teamRepository).findById(teamId);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task result = taskCaptor.getValue();

        assertEquals(team, result.getTeam());
    }

    @Test
    void createTask_managerDifferentTeam(){
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Task task = createTask(taskId);
        Team taskTeam = createTeam(teamId);
        Team userTeam = createTeam(4L);

        task.setTeam(taskTeam);
        user.setTeam(userTeam);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(taskTeam));

        assertThrows(InvalidOperationException.class, () -> taskService.createTask(task));

        verify(teamRepository).findById(teamId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_managerWithoutTeam(){
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);
        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        user.setTeam(null);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        assertThrows(InvalidOperationException.class, () -> taskService.createTask(task));

        verify(teamRepository).findById(teamId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_taskNotFound(){
        Long taskId = 1L;
        TaskUpdate updatedTask = new TaskUpdate();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(taskId, updatedTask));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_adminSuccess() throws AccessDeniedException {
        Long taskId = 1L, userId = 2L;
        TaskUpdate updatedTask = new TaskUpdate();
        Task oldTask = createTask(taskId);

        LocalDateTime time = LocalDateTime.now();

        updatedTask.setTitle("ABC");
        updatedTask.setDescription("abc");
        updatedTask.setStatus(TaskStatus.TODO);
        updatedTask.setPriority(TaskPriority.LOW);
        updatedTask.setDueDate(time);

        mockCurrentUser(UserRole.ADMIN, userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(oldTask));
        when(taskRepository.save(oldTask)).thenReturn(oldTask);

        taskService.updateTask(taskId, updatedTask);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(oldTask);

        assertEquals("ABC", oldTask.getTitle());
        assertEquals("abc", oldTask.getDescription());
        assertEquals(TaskStatus.TODO, oldTask.getStatus());
        assertEquals(TaskPriority.LOW, oldTask.getPriority());
        assertEquals(time, oldTask.getDueDate());
    }

    @Test
    void updateTask_memberOwnTask() throws AccessDeniedException {
        Long taskId = 1L, userId = 2L;
        TaskUpdate updatedTask = new TaskUpdate();
        Task oldTask = createTask(taskId);

        LocalDateTime time = LocalDateTime.now();

        updatedTask.setTitle("ABC");
        updatedTask.setDescription("abc");
        updatedTask.setStatus(TaskStatus.TODO);
        updatedTask.setPriority(TaskPriority.LOW);
        updatedTask.setDueDate(time);

        User user = mockCurrentUser(UserRole.MEMBER, userId);
        user.setEmail("abc@mail.com");

        oldTask.setUser(user);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(oldTask));
        when(taskRepository.save(oldTask)).thenReturn(oldTask);

        taskService.updateTask(taskId, updatedTask);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(oldTask);

        assertEquals("ABC", oldTask.getTitle());
        assertEquals("abc", oldTask.getDescription());
        assertEquals(TaskStatus.TODO, oldTask.getStatus());
        assertEquals(TaskPriority.LOW, oldTask.getPriority());
        assertEquals(time, oldTask.getDueDate());
    }

    @Test
    void updateTask_memberOtherTask(){
        Long taskId = 1L, userId = 2L;
        TaskUpdate updatedTask = new TaskUpdate();
        Task oldTask = createTask(taskId);

        User user = mockCurrentUser(UserRole.MEMBER, userId);
        user.setEmail("abc@mail.com");

        User otherUser = createUser(3L);
        otherUser.setEmail("def@mail.com");

        oldTask.setUser(otherUser);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(oldTask));

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(taskId, updatedTask));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_memberTaskWithoutUser(){
        Long taskId = 1L, userId = 2L;
        TaskUpdate updatedTask = new TaskUpdate();
        Task oldTask = createTask(taskId);

        mockCurrentUser(UserRole.MEMBER, userId);

        oldTask.setUser(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(oldTask));

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(taskId, updatedTask));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_managerSameTeam() throws AccessDeniedException {
        Long taskId = 1L, userId = 2L, teamId = 3L;
        TaskUpdate updatedTask = new TaskUpdate();
        Task oldTask = createTask(taskId);

        Team team = createTeam(teamId);

        LocalDateTime time = LocalDateTime.now();

        updatedTask.setTitle("ABC");
        updatedTask.setDescription("abc");
        updatedTask.setStatus(TaskStatus.TODO);
        updatedTask.setPriority(TaskPriority.LOW);
        updatedTask.setDueDate(time);

        User user = mockCurrentUser(UserRole.MANAGER, userId);
        user.setTeam(team);

        oldTask.setTeam(team);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(oldTask));
        when(taskRepository.save(oldTask)).thenReturn(oldTask);

        taskService.updateTask(taskId, updatedTask);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(oldTask);

        assertEquals("ABC", oldTask.getTitle());
        assertEquals("abc", oldTask.getDescription());
        assertEquals(TaskStatus.TODO, oldTask.getStatus());
        assertEquals(TaskPriority.LOW, oldTask.getPriority());
        assertEquals(time, oldTask.getDueDate());
    }

    @Test
    void updateTask_managerDifferentTeam(){
        Long taskId = 1L, userId = 2L;
        TaskUpdate updatedTask = new TaskUpdate();
        Task oldTask = createTask(taskId);

        User user = mockCurrentUser(UserRole.MANAGER, userId);

        Team team = createTeam(3L);
        Team otherTeam = createTeam(4L);

        user.setTeam(team);
        oldTask.setTeam(otherTeam);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(oldTask));

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(taskId, updatedTask));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTask_managerWithoutTeam(){
        Long taskId = 1L, userId = 2L;
        TaskUpdate updatedTask = new TaskUpdate();
        Task oldTask = createTask(taskId);

        User user = mockCurrentUser(UserRole.MANAGER, userId);
        user.setTeam(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(oldTask));

        assertThrows(AccessDeniedException.class, () -> taskService.updateTask(taskId, updatedTask));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void deleteTask_memberAccessDenied(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);

        assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(taskId));

        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void deleteTask_taskNotFound(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void deleteTask_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Task task = createTask(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_managerSameTeam() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        Team team = createTeam(teamId);

        user.setTeam(team);
        task.setTeam(team);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_managerDifferentTeam(){
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        Team team = createTeam(teamId);
        Team otherTeam = createTeam(4L);

        user.setTeam(team);
        task.setTeam(otherTeam);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void deleteTask_managerWithoutTeam(){
        Long userId = 1L, taskId = 2L;
        User user = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        user.setTeam(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void assignTask_memberAccessDenied(){
        Long userId = 1L, taskId = 2L, currentUserId = 3L;
        mockCurrentUser(UserRole.MEMBER, currentUserId);

        assertThrows(AccessDeniedException.class, () -> taskService.assignTask(taskId, userId));

        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
        verify(userRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void assignTask_taskNotFound(){
        Long userId = 1L, taskId = 2L, currentUserId = 3L;
        mockCurrentUser(UserRole.ADMIN, currentUserId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.assignTask(taskId, userId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verify(userRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void assignTask_userNotFound(){
        Long userId = 1L, taskId = 2L, currentUserId = 3L;
        mockCurrentUser(UserRole.ADMIN, currentUserId);

        Task task = createTask(taskId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.assignTask(taskId, userId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void assignTask_taskWithoutTeam(){
        Long userId = 1L, taskId = 2L, currentUserId = 3L;
        mockCurrentUser(UserRole.ADMIN, currentUserId);

        Task task = createTask(taskId);
        User user = createUser(userId);

        task.setTeam(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> taskService.assignTask(taskId, userId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void assignTask_userWithoutTeam(){
        Long userId = 1L, taskId = 2L, currentUserId = 3L, teamId = 4L;
        mockCurrentUser(UserRole.ADMIN, currentUserId);

        Task task = createTask(taskId);
        User user = createUser(userId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        user.setTeam(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> taskService.assignTask(taskId, userId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void assignTask_differentTeams(){
        Long userId = 1L, taskId = 2L, currentUserId = 3L, teamId = 4L;
        mockCurrentUser(UserRole.ADMIN, currentUserId);

        Task task = createTask(taskId);
        User user = createUser(userId);
        Team team = createTeam(teamId);
        Team otherTeam = createTeam(5L);

        task.setTeam(team);
        user.setTeam(otherTeam);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> taskService.assignTask(taskId, userId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void assignTask_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, currentUserId = 3L, teamId = 4L;
        mockCurrentUser(UserRole.ADMIN, currentUserId);

        Task task = createTask(taskId);
        User user = createUser(userId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        user.setTeam(team);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.assignTask(taskId, userId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
        verify(userRepository).findByIdAndDeletedFalse(userId);

        assertEquals(userId, task.getUser().getId());
    }

    @Test
    void assignTask_managerSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, currentUserId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, currentUserId);

        Task task = createTask(taskId);
        User user = createUser(userId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        user.setTeam(team);
        currentUser.setTeam(team);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.assignTask(taskId, userId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);
        verify(userRepository).findByIdAndDeletedFalse(userId);

        assertEquals(userId, task.getUser().getId());
    }

    @Test
    void assignTask_managerDifferentTeam(){
        Long userId = 1L, taskId = 2L, currentUserId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, currentUserId);

        Task task = createTask(taskId);
        User user = createUser(userId);
        Team team = createTeam(teamId);
        Team otherTeam = createTeam(5L);

        task.setTeam(team);
        user.setTeam(team);
        currentUser.setTeam(otherTeam);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> taskService.assignTask(taskId, userId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void assignTask_managerWithoutTeam(){
        Long userId = 1L, taskId = 2L, currentUserId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, currentUserId);

        Task task = createTask(taskId);
        User user = createUser(userId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        user.setTeam(team);
        currentUser.setTeam(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class, () -> taskService.assignTask(taskId, userId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
        verify(userRepository).findByIdAndDeletedFalse(userId);
    }

    @Test
    void unassignTask_memberAccessDenied(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);

        assertThrows(AccessDeniedException.class, () -> taskService.unassignTask(taskId));

        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void unassignTask_taskNotFound(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.unassignTask(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void unassignTask_alreadyUnassigned(){
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Task task = createTask(taskId);

        task.setUser(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(InvalidOperationException.class, () -> taskService.unassignTask(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void unassignTask_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, otherUserId = 3L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Task task = createTask(taskId);
        User user = createUser(otherUserId);

        task.setUser(user);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.unassignTask(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);

        assertNull(task.getUser());
    }

    @Test
    void unassignTask_managerSameTeam() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, otherUserId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        User user = createUser(otherUserId);
        Team team = createTeam(teamId);

        task.setUser(user);
        currentUser.setTeam(team);
        task.setTeam(team);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.unassignTask(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(task);

        assertNull(task.getUser());
    }

    @Test
    void unassignTask_managerDifferentTeam(){
        Long userId = 1L, taskId = 2L, otherUserId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        User user = createUser(otherUserId);
        Team team = createTeam(teamId);
        Team otherTeam = createTeam(5L);

        task.setUser(user);
        currentUser.setTeam(team);
        task.setTeam(otherTeam);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.unassignTask(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void unassignTask_managerWithoutTeam(){
        Long userId = 1L, taskId = 2L, otherUserId = 3L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        User user = createUser(otherUserId);

        task.setUser(user);
        currentUser.setTeam(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.unassignTask(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any());
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

    private Task createTask(Long id){
        Task task = new Task();
        task.setId(id);

        return task;
    }

    private User createUser(Long id){
        User user = new User();
        user.setId(id);

        return user;
    }

}
