package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.PlaylistTemplate;
import com.in28minutes.webservices.songrec.repository.PlaylistTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistTemplateService {

    private final PlaylistTemplateRepository playlistTemplateRepository;

    @Transactional
    public List<PlaylistTemplate> getBasicPlaylistTemplates() {
        return playlistTemplateRepository.findByIdBetween(1L,9L);
    }
}
