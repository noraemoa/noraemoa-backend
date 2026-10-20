package com.in28minutes.webservices.songrec.service.fileStorage;

import com.in28minutes.webservices.songrec.service.fileStorage.S3FileStorageService.StoredFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * S3를 사용할 수 없는 환경(로컬 개발, 부하 테스트)용 파일 저장소.
 * application.properties 에 storage.type=local 을 넣었을 때만 활성화되고,
 * 그때는 S3FileStorageService 대신 이 빈이 주입된다(@Primary).
 */
@Service
@Primary
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalDiskFileStorageService implements FileStorageService {

  private final Path root = Paths.get("uploads");

  @Override
  public StoredFile storeRequestThumbnail(Long requestId, MultipartFile file) throws IOException {
    return write("requests", requestId + "." + extension(file), file.getBytes());
  }

  @Override
  public StoredFile storePlaylistThumbnail(Long playlistId, MultipartFile file) throws IOException {
    return write("playlists", playlistId + "." + extension(file), file.getBytes());
  }

  @Override
  public StoredFile storeGeneratedThumbnail(Long requestId, byte[] imageBytes) throws IOException {
    return write("requests", requestId + "_thumb.png", imageBytes);
  }

  private StoredFile write(String dir, String fileName, byte[] bytes) throws IOException {
    if (bytes == null || bytes.length == 0) {
      throw new IllegalArgumentException("Empty file");
    }
    Path dirPath = root.resolve(dir);
    Files.createDirectories(dirPath);
    Files.write(dirPath.resolve(fileName), bytes);

    String key = dir + "/" + fileName;
    return new StoredFile(key, "/uploads/" + key);
  }

  private String extension(MultipartFile file) {
    String contentType = file.getContentType();
    if (contentType == null) {
      throw new IllegalArgumentException("Missing content type");
    }
    return switch (contentType.toLowerCase()) {
      case "image/jpeg" -> "jpeg";
      case "image/png" -> "png";
      case "image/gif" -> "gif";
      case "image/webp" -> "webp";
      default -> throw new IllegalArgumentException("Only PNG, JPG, JPEG, WEBP, GIF are allowed");
    };
  }
}