package com.in28minutes.webservices.songrec.service.fileStorage;
import com.in28minutes.webservices.songrec.service.fileStorage.S3FileStorageService.StoredFile;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  StoredFile storeRequestThumbnail(Long requestId, MultipartFile file) throws IOException;
  StoredFile storePlaylistThumbnail(Long playlistId, MultipartFile file) throws IOException;
  StoredFile storeGeneratedThumbnail(Long requestId, byte[] imageBytes) throws IOException;
}
