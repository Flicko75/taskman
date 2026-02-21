package com.flicko.TaskMan.controllers;

import com.flicko.TaskMan.DTOs.CommentCreate;
import com.flicko.TaskMan.DTOs.CommentUpdate;
import com.flicko.TaskMan.models.Comment;
import com.flicko.TaskMan.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/tasks/{taskId}/comments")
    public List<Comment> getAllComments(@PathVariable Long taskId){
        return commentService.getAllComments(taskId);
    }

    @GetMapping("comments/{commentId}")
    public Comment getComment(@PathVariable Long commentId){
        return commentService.getCommentById(commentId);
    }

    @PostMapping("/tasks/{taskId}/comments")
    public Comment addComment(@PathVariable Long taskId, @Valid @RequestBody CommentCreate comment){
        return commentService.addComment(taskId, comment);
    }

    @PutMapping("/comments/{commentId}")
    public Comment updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentUpdate comment){
        return commentService.updateComment(commentId, comment);
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(@PathVariable Long commentId){
        commentService.deleteComment(commentId);
    }

}
