package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    public Long countByRole(UserRole role);

    Optional<User> findByEmail(String email);

}
