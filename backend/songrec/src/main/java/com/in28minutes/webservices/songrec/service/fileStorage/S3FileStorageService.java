package com.in28minutes.webservices.songrec.service.fileStorage;

import com.in28minutes.webservices.songrec.integration.aws.config.S3Properties;
import com.in28minutes.webservices.songrec.service.fileStorage.LocalFileStorageService.StoredFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {
  private final S3Client s3Client;
  private final S3Properties s3Properties;

  @Override
  public StoredFile storeRequestThumbnail(Long requestId, MultipartFile file) throws IOException{
    return storeMultipart("requests", requestId, file);
  }

  @Override
  public StoredFile storePlaylistThumbnail(Long playlistId, MultipartFile file) throws IOException{
    return storeMultipart("playlists", playlistId, file);
  }

  @Override
  public StoredFile storeGeneratedThumbnail(Long requestId, byte[] imageBytes) throws IOException{
    String key ="requests/"+requestId+"_thumb.png";

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key)
        .contentType("image/png")
        .build();

    s3Client.putObject(request,RequestBody.fromBytes(imageBytes));

    String url = s3Properties.getBaseUrl()+"/"+key;
    return new StoredFile(key,url);
  }

  private StoredFile storeMultipart(String dir, Long id, MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Empty file");
    }

    String contentType = file.getContentType();
    if (contentType == null) {
      throw new IllegalArgumentException("Missing content type");
    }

    String extension = switch (contentType.toLowerCase()){
      case "image/jpeg" -> "jpeg";
      case "image/png" -> "png";
      case "image/gif" -> "gif";
      case "image/webp" -> "webp";
      default -> throw new IllegalArgumentException("Only PNG, JPG, JPEG, WEBP, GIF are allowed");
    };

    // S3에 넣음.
    String key = dir + "/" + id + "." + extension;
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(s3Properties.getBucket())
        .key(key)
        .contentType(contentType)
        .build();

    s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

    String url = s3Properties.getBaseUrl() + "/" + key;
    return new StoredFile(key, url);
  }

  public record StoredFile(String key, String url) {}
}
