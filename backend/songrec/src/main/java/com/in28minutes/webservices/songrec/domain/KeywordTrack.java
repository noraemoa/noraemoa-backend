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

}
