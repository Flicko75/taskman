package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
