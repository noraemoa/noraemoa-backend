package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Playlist;
import com.in28minutes.webservices.songrec.domain.PlaylistTemplate;
import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.User;
import com.in28minutes.webservices.songrec.dto.request.PlaylistCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.PlaylistRepository;
import com.in28minutes.webservices.songrec.repository.PlaylistTemplateRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTemplateService playlistTemplateService;
    private final EntityManager entityManager;
    private final LocalFileStorageService localFileStorageService;

    @Transactional
    public void createBasicPlaylists(Long userId) {
        User userRef=entityManager.getReference(User.class, userId);
        List<PlaylistTemplate> playlistTemplates = playlistTemplateService.getBasicPlaylistTemplates();
        for(PlaylistTemplate t :playlistTemplates) {
            if (!playlistRepository.existsByUser_IdAndTemplate_Id(userId, t.getId())) {
                playlistRepository.save(Playlist
                        .builder()
                        .user(userRef)
                        .template(t)
                        .title(t.getDisplayName())
                        .deleted(false)
                        .build()
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Playlist> getPlaylistsByUserId(Long userId) {
        return playlistRepository.findAllByUserIdAndDeletedFalse(userId);
    }

    @Transactional
    public Playlist createPlaylist(Long userId, PlaylistCreateRequestDto playlistDto){
        User userRef=entityManager.getReference(User.class, userId);
        Playlist playlist =  Playlist.builder()
                .user(userRef)
                .title(playlistDto.getTitle())
                .deleted(false)
                .build();

        return playlistRepository.save(playlist);
    }

    @Transactional(readOnly = true)
    public Playlist getActivePlaylist(Long userId, Long playlistId){
        return playlistRepository.findByIdAndUserIdAndDeletedFalse(playlistId,userId)
                .orElseThrow(()->new NotFoundException("해당 요청을 찾을 수 없습니다."));
    }

    @Transactional
    public Playlist updatePlaylist(PlaylistCreateRequestDto playlistDto, Long userId, Long playlistId) {
        Playlist playlist = getActivePlaylist(userId,playlistId);
        playlist.setTitle(playlistDto.getTitle());
        return playlist;
    }

    @Transactional
    public void deletePlaylist(Long userId, Long playlistId) {
        Playlist playlist = getActivePlaylist(userId,playlistId);
        playlist.setDeleted(true);
    }

    @Transactional
    public Playlist uploadThumbnail(Long userId, Long playlistId, MultipartFile file) throws IOException {
        Playlist playlist = getActivePlaylist(userId,playlistId);

        LocalFileStorageService.StoredFile stored =
                localFileStorageService.storePlaylistThumbnail(playlistId,file);

        playlist.setThumbnailKey(stored.key());
        playlist.setThumbnailUrl(stored.url());
        return playlist;
    }
}
