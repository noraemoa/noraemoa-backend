package com.in28minutes.webservices.songrec.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_keyword",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"request_id", "keyword_id"})
        })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RequestKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id",nullable = false)
    private Request request;

    @ManyToOne(optional = false)
    @JoinColumn(name = "keyword_id",nullable = false)
    private Keyword keyword;

}
