package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_text")
    private String rawText;
    @Column(name = "normalized_text")
    private String normalizedText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_id")
    private Keyword canonicalId;

}
