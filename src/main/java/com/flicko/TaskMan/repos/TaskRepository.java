package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
