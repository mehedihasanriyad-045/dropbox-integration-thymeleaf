package com.iis.dropboxthymelife.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "APP_USER")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column (name = "ID")
    private Long id;

    @Column (name = "NAME")
    private String name;

    @Column (name = "EMAIL")
    private String email;

    @Column (name = "ACCESS_TOKEN")
    private String accessToken;

    @Column (name = "REFRESH_TOKEN")
    private String refreshToken;

    @Column(name = "EXPIRES_AT")
    private long expiresAt;

}
