package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.CommentCreate;
import com.flicko.TaskMan.DTOs.CommentResponse;
import com.flicko.TaskMan.DTOs.CommentUpdate;
import com.flicko.TaskMan.DTOs.PageResponse;
import com.flicko.TaskMan.models.Comment;
import com.flicko.TaskMan.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/tasks/{taskId}/comments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public PageResponse<CommentResponse> getAllComments(@PathVariable Long taskId, Pageable pageable) throws AccessDeniedException {
        return commentService.getAllComments(taskId, pageable);
    }

    @GetMapping("comments/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public CommentResponse getComment(@PathVariable Long commentId) throws AccessDeniedException {
        return commentService.getCommentById(commentId);
    }

    @PostMapping("/tasks/{taskId}/comments")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public CommentResponse addComment(@PathVariable Long taskId, @Valid @RequestBody CommentCreate comment) throws AccessDeniedException {
        return commentService.addComment(taskId, comment);
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public CommentResponse updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentUpdate comment) throws AccessDeniedException {
        return commentService.updateComment(commentId, comment);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MEMBER')")
    public void deleteComment(@PathVariable Long commentId) throws AccessDeniedException {
        commentService.deleteComment(commentId);
    }

}
