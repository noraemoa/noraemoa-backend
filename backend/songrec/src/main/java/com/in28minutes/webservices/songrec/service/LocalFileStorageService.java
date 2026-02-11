package com.in28minutes.webservices.songrec.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileStorageService {

    private final Path root = Paths.get("uploads"); // 프로젝트 루트/uploads

    public StoredFile storeRequestThumbnail(Long requestId, MultipartFile file) throws IOException {
        return storePng("requests", requestId, file);
    }

    public StoredFile storePlaylistThumbnail(Long playlistId, MultipartFile file) throws IOException {
        return storePng("playlists", playlistId, file);
    }

    private StoredFile storePng(String dir, Long id, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("image/png")) {
            throw new IllegalArgumentException("PNG only");
        }

        Path rootAbs = root.toAbsolutePath().normalize();

        String key = dir + "/" + id + ".png";
        Path target = rootAbs.resolve(key).normalize();

        // ✅ 디렉토리 탈출 방지 (normalize만으로는 100% 아님)
        if (!target.startsWith(rootAbs)) {
            throw new IllegalArgumentException("Invalid path");
        }

        Files.createDirectories(target.getParent());

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String url = "/uploads/" + key;
        return new StoredFile(key, url);
    }

    public record StoredFile(String key, String url) {}
}
