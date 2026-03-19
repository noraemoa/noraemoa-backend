package com.in28minutes.webservices.songrec.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class LocalFileStorageService {

    private final Path root = Paths.get("uploads");

    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
        "image/png", "png",
        "image/jpeg", "jpg",
        "image/webp", "webp",
        "image/gif", "gif"
    );

    public StoredFile storeRequestThumbnail(Long requestId, MultipartFile file) throws IOException {
        return storeImage("requests", requestId, file);
    }

    public StoredFile storePlaylistThumbnail(Long playlistId, MultipartFile file) throws IOException {
        return storeImage("playlists", playlistId, file);
    }

    private StoredFile storeImage(String dir, Long id, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Missing content type");
        }

        String normalizedContentType = contentType.toLowerCase();
        String extension = ALLOWED_IMAGE_TYPES.get(normalizedContentType);

        if (extension == null) {
            throw new IllegalArgumentException("Only PNG, JPG, JPEG, WEBP, GIF are allowed");
        }

        Path rootAbs = root.toAbsolutePath().normalize();
        Path dirPath = rootAbs.resolve(dir).normalize();

        if (!dirPath.startsWith(rootAbs)) {
            throw new IllegalArgumentException("Invalid directory path");
        }

        Files.createDirectories(dirPath);

        deleteExistingFiles(dirPath, id);

        String key = dir + "/" + id + "." + extension;
        Path target = rootAbs.resolve(key).normalize();

        if (!target.startsWith(rootAbs)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String url = "/uploads/" + key;
        return new StoredFile(key, url);
    }

    private void deleteExistingFiles(Path dirPath, Long id) throws IOException {
        try (Stream<Path> stream = Files.list(dirPath)) {
            stream.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith(id + "."))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    public record StoredFile(String key, String url) {}

    public StoredFile storeGeneratedThumbnail(Long requestId, byte[] imageBytes) throws IOException {
        String fileName = requestId + "_thumb.png";

        Path path = Paths.get("uploads/requests/" + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, imageBytes);

        String url = "/uploads/requests/" + fileName;

        return new StoredFile(fileName, url);
    }
}