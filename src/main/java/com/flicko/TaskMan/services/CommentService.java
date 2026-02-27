package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.CommentCreate;
import com.flicko.TaskMan.DTOs.CommentResponse;
import com.flicko.TaskMan.DTOs.CommentUpdate;
import com.flicko.TaskMan.DTOs.PageResponse;
import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Comment;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.CommentRepository;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.UserRepository;
import com.flicko.TaskMan.utils.PageMapper;
import com.flicko.TaskMan.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final SecurityUtils securityUtils;

    public PageResponse<CommentResponse> getAllComments(Long taskId, Pageable pageable) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        Page<Comment> comments;
        if (user.getRole() == UserRole.ADMIN){
            comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId, pageable);
        } else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null ||
                task.getTeam() == null ||
                !task.getTeam().getId().equals(user.getTeam().getId()))
                throw new AccessDeniedException("Can't fetch comments of other teams");
            comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId, pageable);
        } else if (user.getRole() == UserRole.MEMBER) {
            if (task.getUser() == null ||
                !task.getUser().getId().equals(user.getId()))
                throw new AccessDeniedException("Can't fetch comments of unassigned tasks");
            comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId, pageable);
        }else
            throw new AccessDeniedException("Access Denied");

        Page<CommentResponse> mapped = comments.map(this::mapToResponse);

        return PageMapper.toPageResponse(mapped);
    }

    public CommentResponse getCommentById(Long commentId) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Task task = comment.getTask();

        if (user.getRole() == UserRole.ADMIN){

        } else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null ||
                task.getTeam() == null ||
                !task.getTeam().getId().equals(user.getTeam().getId()))
                throw new AccessDeniedException("Can't fetch comments of other teams");
        } else if (user.getRole() == UserRole.MEMBER) {
            if (task.getUser() == null ||
                !task.getUser().getId().equals(user.getId()))
                throw new AccessDeniedException("Can't fetch comments of unassigned tasks");
        } else
            throw new AccessDeniedException("Access Denied");

        return mapToResponse(comment);
    }

    @Transactional
    public CommentResponse addComment(Long taskId, @Valid CommentCreate comment) throws AccessDeniedException {
        User currentUser = securityUtils.getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (currentUser.getRole() == UserRole.ADMIN){

        } else if (currentUser.getRole() == UserRole.MANAGER) {
            if (currentUser.getTeam() == null ||
                task.getTeam() == null ||
                !task.getTeam().getId().equals(currentUser.getTeam().getId()))
                throw new AccessDeniedException("Can't add comment to other team tasks");
        } else if (currentUser.getRole() == UserRole.MEMBER) {
            if (task.getUser() == null ||
                !task.getUser().getId().equals(currentUser.getId()))
                throw new AccessDeniedException("Can't add comment to unassigned tasks");
        }else
            throw new AccessDeniedException("Access Denied");

        Comment comment1 = new Comment();

        comment1.setUser(currentUser);
        comment1.setTask(task);
        comment1.setContent(comment.getContent());

        return mapToResponse(commentRepository.save(comment1));
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, @Valid CommentUpdate comment) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        Comment oldcomment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Task task = oldcomment.getTask();

        if (user.getRole() == UserRole.ADMIN){

        } else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null ||
                task.getTeam() == null ||
                !task.getTeam().getId().equals(user.getTeam().getId()))
                throw new AccessDeniedException("Can't update comments of other teams");
        } else if (user.getRole() == UserRole.MEMBER) {
            if (oldcomment.getUser() == null ||
                !oldcomment.getUser().getId().equals(user.getId()))
                throw new AccessDeniedException("Can't update comments of other members");
        } else
            throw new AccessDeniedException("Access Denied");

        oldcomment.setContent(comment.getContent());

        return mapToResponse(commentRepository.save(oldcomment));
    }

    @Transactional
    public void deleteComment(Long commentId) throws AccessDeniedException {
        User user = securityUtils.getCurrentUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Task task = comment.getTask();

        if (user.getRole() == UserRole.ADMIN){

        } else if (user.getRole() == UserRole.MANAGER) {
            if (user.getTeam() == null ||
                task.getTeam() == null ||
                !task.getTeam().getId().equals(user.getTeam().getId()))
                throw new AccessDeniedException("Can't delete comments of other teams");
        } else if (user.getRole() == UserRole.MEMBER) {
            if (comment.getUser() == null ||
                !comment.getUser().getId().equals(user.getId()))
                throw new AccessDeniedException("Can't delete comments of other members");
        } else
            throw new AccessDeniedException("Access Denied");

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(Comment comment){
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getTask() != null ? comment.getTask().getId() : null,
                comment.getUser() != null ? comment.getUser().getId() : null
        );
    }
}
