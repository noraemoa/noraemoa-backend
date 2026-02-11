package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "playlist_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MORNING, LUNCH, THIS_MONTH, SPRING ... 같은 “시스템 코드”
    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String displayName;
}