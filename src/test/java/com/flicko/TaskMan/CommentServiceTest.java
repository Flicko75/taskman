package com.flicko.TaskMan;

import com.flicko.TaskMan.DTOs.CommentCreate;
import com.flicko.TaskMan.DTOs.CommentResponse;
import com.flicko.TaskMan.DTOs.CommentUpdate;
import com.flicko.TaskMan.DTOs.PageResponse;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Comment;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.Team;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.CommentRepository;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.services.CommentService;
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

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getAllComments_taskNotFound() {
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Pageable pageable = PageRequest.of(0, 4);

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getAllComments(taskId, pageable));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).findByTaskIdOrderByCreatedAtAsc(any(), any());
    }

    @Test
    void getAllComments_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task = createTask(taskId);

        Comment comment1 = new Comment();
        Comment comment2 = new Comment();

        Page<Comment> page = new PageImpl<>(List.of(comment1, comment2));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId, pageable)).thenReturn(page);

        PageResponse<CommentResponse> result = commentService.getAllComments(taskId, pageable);

        assertEquals(2, result.content().size());

        verify(taskRepository).findById(taskId);
        verify(commentRepository).findByTaskIdOrderByCreatedAtAsc(taskId, pageable);
    }

    @Test
    void getAllComments_managerSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        currentUser.setTeam(team);

        Comment comment = new Comment();

        Page<Comment> page = new PageImpl<>(List.of(comment));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId, pageable)).thenReturn(page);

        PageResponse<CommentResponse> result = commentService.getAllComments(taskId, pageable);

        assertEquals(1, result.content().size());

        verify(taskRepository).findById(taskId);
        verify(commentRepository).findByTaskIdOrderByCreatedAtAsc(taskId, pageable);
    }

    @Test
    void getAllComments_managerDifferentTeam() {
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task = createTask(taskId);

        Team team = createTeam(teamId);
        Team otherTeam = createTeam(4L);

        currentUser.setTeam(team);
        task.setTeam(otherTeam);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> commentService.getAllComments(taskId, pageable));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).findByTaskIdOrderByCreatedAtAsc(any(), any());
    }

    @Test
    void getAllComments_managerWithoutTeam() {
        Long userId = 1L, taskId = 2L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task = createTask(taskId);

        currentUser.setTeam(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> commentService.getAllComments(taskId, pageable));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).findByTaskIdOrderByCreatedAtAsc(any(), any());
    }

    @Test
    void getAllComments_memberSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L;
        User currentUser = mockCurrentUser(UserRole.MEMBER, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task = createTask(taskId);
        task.setUser(currentUser);

        Comment comment = new Comment();

        Page<Comment> page = new PageImpl<>(List.of(comment));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId, pageable)).thenReturn(page);

        PageResponse<CommentResponse> result = commentService.getAllComments(taskId, pageable);

        assertEquals(1, result.content().size());

        verify(taskRepository).findById(taskId);
        verify(commentRepository).findByTaskIdOrderByCreatedAtAsc(taskId, pageable);
    }

    @Test
    void getAllComments_memberTaskWithoutUser() {
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task = createTask(taskId);
        task.setUser(null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> commentService.getAllComments(taskId, pageable));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).findByTaskIdOrderByCreatedAtAsc(any(), any());
    }

    @Test
    void getAllComments_memberDifferentUser() {
        Long userId = 1L, taskId = 2L, otherUserId = 3L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Pageable pageable = PageRequest.of(0, 4);

        Task task = createTask(taskId);
        User otherUser = createUser(otherUserId);

        task.setUser(otherUser);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> commentService.getAllComments(taskId, pageable));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).findByTaskIdOrderByCreatedAtAsc(any(), any());
    }

    @Test
    void getCommentById_commentNotFound() {
        Long userId = 1L, commentId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById(commentId));

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L, taskId = 3L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Task task = createTask(taskId);
        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.getCommentById(commentId);

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_managerSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L, taskId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        currentUser.setTeam(team);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.getCommentById(commentId);

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_managerDifferentTeam() {
        Long userId = 1L, commentId = 2L, taskId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        Team team = createTeam(teamId);
        Team otherTeam = createTeam(5L);

        currentUser.setTeam(team);
        task.setTeam(otherTeam);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.getCommentById(commentId));

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_managerWithoutTeam() {
        Long userId = 1L, commentId = 2L, taskId = 3L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        currentUser.setTeam(null);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.getCommentById(commentId));

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_memberSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L, taskId = 3L;
        User currentUser = mockCurrentUser(UserRole.MEMBER, userId);

        Task task = createTask(taskId);
        task.setUser(currentUser);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.getCommentById(commentId);

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_memberTaskWithoutUser() {
        Long userId = 1L, commentId = 2L, taskId = 3L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Task task = createTask(taskId);
        task.setUser(null);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.getCommentById(commentId));

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_memberDifferentUser() {
        Long userId = 1L, commentId = 2L, taskId = 3L, otherUserId = 4L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Task task = createTask(taskId);
        User otherUser = createUser(otherUserId);
        task.setUser(otherUser);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.getCommentById(commentId));

        verify(commentRepository).findById(commentId);
    }

    @Test
    void addComment_taskNotFound() {
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        CommentCreate comment = new CommentCreate();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.addComment(taskId, comment));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L;
        User currentUser = mockCurrentUser(UserRole.ADMIN, userId);

        Task task = createTask(taskId);

        CommentCreate comment = new CommentCreate();
        comment.setContent("test");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        commentService.addComment(taskId, comment);

        verify(taskRepository).findById(taskId);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());

        Comment result = captor.getValue();

        assertEquals(currentUser, result.getUser());
        assertEquals(task, result.getTask());
        assertEquals("test", result.getContent());
    }

    @Test
    void addComment_managerSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        currentUser.setTeam(team);
        task.setTeam(team);

        CommentCreate comment = new CommentCreate();
        comment.setContent("test");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        commentService.addComment(taskId, comment);

        verify(taskRepository).findById(taskId);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());

        Comment result = captor.getValue();

        assertEquals(currentUser, result.getUser());
        assertEquals(task, result.getTask());
        assertEquals("test", result.getContent());
    }

    @Test
    void addComment_managerDifferentTeam() {
        Long userId = 1L, taskId = 2L, teamId = 3L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        Team team = createTeam(teamId);
        Team otherTeam = createTeam(4L);

        currentUser.setTeam(team);
        task.setTeam(otherTeam);

        CommentCreate comment = new CommentCreate();
        comment.setContent("test");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> commentService.addComment(taskId, comment));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_managerWithoutTeam() {
        Long userId = 1L, taskId = 2L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        currentUser.setTeam(null);

        CommentCreate comment = new CommentCreate();
        comment.setContent("test");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> commentService.addComment(taskId, comment));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_memberSuccess() throws AccessDeniedException {
        Long userId = 1L, taskId = 2L;
        User currentUser = mockCurrentUser(UserRole.MEMBER, userId);

        Task task = createTask(taskId);
        task.setUser(currentUser);

        CommentCreate comment = new CommentCreate();
        comment.setContent("test");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        commentService.addComment(taskId, comment);

        verify(taskRepository).findById(taskId);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());

        Comment result = captor.getValue();

        assertEquals(currentUser, result.getUser());
        assertEquals(task, result.getTask());
        assertEquals("test", result.getContent());
    }

    @Test
    void addComment_memberTaskWithoutUser() {
        Long userId = 1L, taskId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Task task = createTask(taskId);
        task.setUser(null);

        CommentCreate comment = new CommentCreate();
        comment.setContent("test");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> commentService.addComment(taskId, comment));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_memberDifferentUser() {
        Long userId = 1L, taskId = 2L, otherUserId = 3L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Task task = createTask(taskId);
        User otherUser = createUser(otherUserId);

        task.setUser(otherUser);

        CommentCreate comment = new CommentCreate();
        comment.setContent("test");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> commentService.addComment(taskId, comment));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_commentNotFound() {
        Long userId = 1L, commentId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        CommentUpdate comment = new CommentUpdate();

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(commentId, comment));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L, taskId = 3L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Task task = createTask(taskId);
        Comment oldComment = createComment(commentId);
        oldComment.setTask(task);

        CommentUpdate comment = new CommentUpdate();
        comment.setContent("updated");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(oldComment));
        when(commentRepository.save(oldComment)).thenReturn(oldComment);

        commentService.updateComment(commentId, comment);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(oldComment);

        assertEquals("updated", oldComment.getContent());
    }

    @Test
    void updateComment_managerSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L, taskId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        currentUser.setTeam(team);

        Comment oldComment = createComment(commentId);
        oldComment.setTask(task);

        CommentUpdate comment = new CommentUpdate();
        comment.setContent("updated");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(oldComment));
        when(commentRepository.save(oldComment)).thenReturn(oldComment);

        commentService.updateComment(commentId, comment);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(oldComment);

        assertEquals("updated", oldComment.getContent());
    }

    @Test
    void updateComment_managerDifferentTeam() {
        Long userId = 1L, commentId = 2L, taskId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        Team team = createTeam(teamId);
        Team otherTeam = createTeam(5L);

        currentUser.setTeam(team);
        task.setTeam(otherTeam);

        Comment oldComment = createComment(commentId);
        oldComment.setTask(task);

        CommentUpdate comment = new CommentUpdate();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(oldComment));

        assertThrows(AccessDeniedException.class, () -> commentService.updateComment(commentId, comment));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_managerWithoutTeam() {
        Long userId = 1L, commentId = 2L, taskId = 3L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        currentUser.setTeam(null);

        Comment oldComment = createComment(commentId);
        oldComment.setTask(task);

        CommentUpdate comment = new CommentUpdate();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(oldComment));

        assertThrows(AccessDeniedException.class, () -> commentService.updateComment(commentId, comment));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_memberSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L;
        User currentUser = mockCurrentUser(UserRole.MEMBER, userId);

        Comment oldComment = createComment(commentId);
        oldComment.setUser(currentUser);

        CommentUpdate comment = new CommentUpdate();
        comment.setContent("updated");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(oldComment));
        when(commentRepository.save(oldComment)).thenReturn(oldComment);

        commentService.updateComment(commentId, comment);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(oldComment);

        assertEquals("updated", oldComment.getContent());
    }

    @Test
    void updateComment_memberCommentWithoutUser() {
        Long userId = 1L, commentId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Comment oldComment = createComment(commentId);
        oldComment.setUser(null);

        CommentUpdate comment = new CommentUpdate();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(oldComment));

        assertThrows(AccessDeniedException.class, () -> commentService.updateComment(commentId, comment));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_memberDifferentUser() {
        Long userId = 1L, commentId = 2L, otherUserId = 3L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Comment oldComment = createComment(commentId);
        User otherUser = createUser(otherUserId);

        oldComment.setUser(otherUser);

        CommentUpdate comment = new CommentUpdate();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(oldComment));

        assertThrows(AccessDeniedException.class, () -> commentService.updateComment(commentId, comment));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_commentNotFound() {
        Long userId = 1L, commentId = 2L;
        mockCurrentUser(UserRole.ADMIN, userId);

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(commentId));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_adminSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L, taskId = 3L;
        mockCurrentUser(UserRole.ADMIN, userId);

        Task task = createTask(taskId);
        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_managerSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L, taskId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        Team team = createTeam(teamId);

        task.setTeam(team);
        currentUser.setTeam(team);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_managerDifferentTeam() {
        Long userId = 1L, commentId = 2L, taskId = 3L, teamId = 4L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);

        Team team = createTeam(teamId);
        Team otherTeam = createTeam(5L);

        currentUser.setTeam(team);
        task.setTeam(otherTeam);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(commentId));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_managerWithoutTeam() {
        Long userId = 1L, commentId = 2L, taskId = 3L;
        User currentUser = mockCurrentUser(UserRole.MANAGER, userId);

        Task task = createTask(taskId);
        currentUser.setTeam(null);

        Comment comment = createComment(commentId);
        comment.setTask(task);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(commentId));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_memberSuccess() throws AccessDeniedException {
        Long userId = 1L, commentId = 2L;
        User currentUser = mockCurrentUser(UserRole.MEMBER, userId);

        Comment comment = createComment(commentId);
        comment.setUser(currentUser);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_memberCommentWithoutUser() {
        Long userId = 1L, commentId = 2L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Comment comment = createComment(commentId);
        comment.setUser(null);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(commentId));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_memberDifferentUser() {
        Long userId = 1L, commentId = 2L, otherUserId = 3L;
        mockCurrentUser(UserRole.MEMBER, userId);

        Comment comment = createComment(commentId);
        User otherUser = createUser(otherUserId);

        comment.setUser(otherUser);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(commentId));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    private User mockCurrentUser(UserRole role, Long id){
        User user = new User();
        user.setId(id);
        user.setRole(role);

        when(securityUtils.getCurrentUser()).thenReturn(user);

        return user;
    }

    private Task createTask(Long id){
        Task task = new Task();
        task.setId(id);

        return task;
    }

    private Comment createComment(Long id){
        Comment comment = new Comment();
        comment.setId(id);

        return comment;
    }

    private Team createTeam(Long id){
        Team team = new Team();
        team.setId(id);

        return team;
    }

    private User createUser(Long id){
        User user = new User();
        user.setId(id);

        return user;
    }

}
