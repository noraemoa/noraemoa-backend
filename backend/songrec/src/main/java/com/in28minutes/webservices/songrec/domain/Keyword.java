package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keywords")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_text",nullable = false)
    private String rawText;
    @Column(name = "normalized_text",nullable = false,unique = true)
    private String normalizedText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_id")
    private Keyword canonical;

}
