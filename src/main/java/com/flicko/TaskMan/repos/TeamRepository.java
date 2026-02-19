package com.flicko.TaskMan.repos;

import com.flicko.TaskMan.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    public boolean existsByName(String name);

    public boolean existsByNameAndIdNot(String name, Long id);

}
