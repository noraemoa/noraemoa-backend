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
        // 1) 파일 타입 최소 검증
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
        if (file.getContentType() == null || !file.getContentType().equals("image/png")) {
            throw new IllegalArgumentException("PNG only");
        }

        // 2) 저장 경로
        String key = "requests/" + requestId + ".png"; // DB에 넣을 key
        Path target = root.resolve(key).normalize();   // uploads/requests/{id}.png

        Files.createDirectories(target.getParent());

        // 3) 덮어쓰기 저장
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // 4) 프론트가 접근할 URL(상대경로)
        String url = "/uploads/" + key;

        return new StoredFile(key, url);
    }

    public record StoredFile(String key, String url) {}
}
