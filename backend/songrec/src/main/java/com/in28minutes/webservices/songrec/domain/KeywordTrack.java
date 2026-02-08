package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword_track",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"track_id", "keyword_id"})
        })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class KeywordTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @ManyToOne(optional = false)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    // 카운트가 올라가는 경로:
    // 1. 노래를 재생하는 중 키워드를 선택했을 때.
    // 2. 사용자가 본인 request에 노래를 직접 입력했을 때.
    @Builder.Default
    private Integer recommendCount=0;

    @Builder.Default
    @Column(nullable = false)
    private Integer ratingCount=0;

    @Builder.Default
    @Column(nullable = false)
    private Integer ratingSum=0;

    @Builder.Default
    @Column(nullable = false)
    private double ratingAverage=0.0;
}
