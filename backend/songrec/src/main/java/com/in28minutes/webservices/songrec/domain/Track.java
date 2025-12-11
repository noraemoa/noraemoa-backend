package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tracks")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spotify_id", nullable = false,unique = true)
    private String spotifyId;

    @Column(nullable = false)
    private String name;

    private String artist;
    private String album;
}
