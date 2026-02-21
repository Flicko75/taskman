package com.flicko.TaskMan.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flicko.TaskMan.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @JsonIgnore
    @NotBlank
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Task> tasks;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Comment> comments;

}
