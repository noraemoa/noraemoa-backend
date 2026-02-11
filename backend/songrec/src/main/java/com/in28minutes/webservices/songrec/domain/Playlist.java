package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "playlists",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "template_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    // 기본 틀 기반 플리면 값이 있고, 커스텀 플리면 null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="template_id")
    private PlaylistTemplate template;

    // 커스텀 플리 이름 / 또는 기본 플리도 표시명 커스텀 허용 시 사용
    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name="thumbnail_key")
    private String thumbnailKey;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
}
