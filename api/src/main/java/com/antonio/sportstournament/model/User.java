package com.antonio.sportstournament.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="user_id")
    private Long id;

    @Column(nullable = false,length=100)
    private String name;

    @Column(nullable = false,unique = true,length = 255)
    private String email;

    @Column(nullable = false,name="password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Role role = Role.TEAM_MANAGER;

    @Column(name="two_factor_secret",length=64)
    private String twoFactorSecret;

    @Column(name="two_factor_enabled",nullable = false)
    private boolean twoFactorEnabled =false;

    @Column(nullable = false,name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
