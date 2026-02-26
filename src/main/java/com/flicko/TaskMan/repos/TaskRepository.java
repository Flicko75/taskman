package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.models.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    public List<Task> findByUserId(Long userId);

    public Page<Task> findByUserId(Long userId, Pageable pageable);

    public Page<Task> findByTeamId(Long teamId, Pageable pageable);

}
