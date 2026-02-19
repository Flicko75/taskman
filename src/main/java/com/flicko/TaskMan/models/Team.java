package com.flicko.TaskMan.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    @PrePersist
    public void setCreatedAt(){
        this.createdAt = LocalDateTime.now();
    }

    @JsonIgnore
    @OneToMany(mappedBy = "team")
    private List<User> users;

    @JsonIgnore
    @OneToMany(mappedBy = "team")
    private List<Task> tasks;

}
