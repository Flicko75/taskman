package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
