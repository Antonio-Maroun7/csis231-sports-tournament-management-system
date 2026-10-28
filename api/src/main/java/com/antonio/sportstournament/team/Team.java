package com.antonio.sportstournament.team;

import com.antonio.sportstournament.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(name = "team_name",nullable = false,length = 100)
    private String teamName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="manager_id",nullable = false)
    private User manager;

    @Column(name="created_at",nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
