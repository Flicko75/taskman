package com.flicko.TaskMan.services;

import com.flicko.TaskMan.DTOs.CommentCreate;
import com.flicko.TaskMan.DTOs.CommentResponse;
import com.flicko.TaskMan.DTOs.CommentUpdate;
import com.flicko.TaskMan.exceptions.InvalidOperationException;
import com.flicko.TaskMan.exceptions.ResourceNotFoundException;
import com.flicko.TaskMan.models.Comment;
import com.flicko.TaskMan.models.Task;
import com.flicko.TaskMan.models.User;
import com.flicko.TaskMan.repos.CommentRepository;
import com.flicko.TaskMan.repos.TaskRepository;
import com.flicko.TaskMan.repos.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    public List<CommentResponse> getAllComments(Long taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        List<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);

        return comments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        return mapToResponse(comment);
    }

    @Transactional
    public CommentResponse addComment(Long taskId, @Valid CommentCreate comment) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User user = userRepository.findById(comment.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getTeam() == null
                || task.getTeam() == null
                || !user.getTeam().getId().equals(task.getTeam().getId()))
            throw new InvalidOperationException("User and Task needs to belong to same team");

        Comment comment1 = new Comment();

        comment1.setUser(user);
        comment1.setTask(task);
        comment1.setContent(comment.getContent());

        return mapToResponse(commentRepository.save(comment1));
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, @Valid CommentUpdate comment) {
        Comment oldcomment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        oldcomment.setContent(comment.getContent());

        return mapToResponse(commentRepository.save(oldcomment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

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
