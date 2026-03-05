package com.flicko.TaskMan.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flicko.TaskMan.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "users",
        indexes = {
            @Index(name = "idx_user_deleted", columnList = "deleted"),
            @Index(name = "idx_user_role", columnList = "role"),
            @Index(name = "idx_user_team_deleted", columnList = "team_id, deleted")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

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

    private boolean deleted = false;

    private int tokenVersion = 0;

    private LocalDateTime lastLoginAt;

}
