package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    public List<Task> findByUserId(Long userId);

}
