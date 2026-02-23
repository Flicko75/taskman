package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId, Pageable pageable);

    List<Comment> findByUserId(Long id);
}
