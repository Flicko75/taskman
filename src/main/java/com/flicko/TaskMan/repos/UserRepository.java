package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.enums.UserRole;
import com.flicko.TaskMan.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Long countByRole(UserRole role);

    Long countByRoleAndDeletedFalse(UserRole role);

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Page<User> findByDeletedFalse(Pageable pageable);

    Page<User> findByTeamIdAndDeletedFalse(Long id, Pageable pageable);

}
