package com.in28minutes.webservices.songrec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.domain.keyword.Keyword;
import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.KeywordCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.request.RequestResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.integration.openai.dto.RequestPromptRefineResult;
import com.in28minutes.webservices.songrec.repository.RequestRepository;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import com.in28minutes.webservices.songrec.service.fileStorage.LocalFileStorageService;
import com.in28minutes.webservices.songrec.service.fileStorage.LocalFileStorageService.StoredFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {

  @InjectMocks
  private RequestService requestService;

  @Mock
  private RequestRepository requestRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private RequestPromptAiService requestPromptAiService;
  @Mock
  private RequestThumbnailAiService requestThumbnailAiService;
  @Mock
  private RequestKeywordService requestKeywordService;
  @Mock
  private KeywordService keywordService;
  @Mock
  private RequestTrackService requestTrackService;
  @Mock
  private KeywordTrackService keywordTrackService;
  @Mock
  private LocalFileStorageService localFileStorageService;

  @Test
  @DisplayName("사용자의 요청이 아닐 경우 예외를 던진다.")
  void updateRequest_whenNotOwned_thenThrowException() {
    Long userId = 1L;
    Long requestId = 1L;
    RequestCreateRequestDto dto = RequestCreateRequestDto.builder().prompt("test prompt").build();

    when(requestRepository.findByIdAndUserIdAndDeletedFalse(requestId, userId)).thenReturn(
        Optional.empty());

    assertThatThrownBy(() -> requestService.updateRequest(dto, userId, requestId)).isInstanceOf(
        NotFoundException.class).hasMessage("해당 요청을 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("사용자의 요청이 아닐 경우 예외를 던진다.")
  void deleteRequest_whenNotOwned_thenThrowException() {
    Long userId = 1L;
    Long requestId = 1L;
    when(requestRepository.findByIdAndUserIdAndDeletedFalse(requestId, userId)).thenReturn(
        Optional.empty());

    assertThatThrownBy(() -> requestService.deleteRequest(userId, requestId)).isInstanceOf(
        NotFoundException.class).hasMessage("해당 요청을 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("이미 삭제된 요청인 경우 예외를 던진다.")
  void deleteRequestAdmin_whenDeleted_thenThrowException() {
    Long userId = 1L;
    Long requestId = 1L;
    when(requestRepository.findByIdAndDeletedFalse(requestId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> requestService.deleteRequestAdmin(requestId)).isInstanceOf(
        NotFoundException.class).hasMessage("해당 요청을 찾을 수 없습니다.");
  }

  @Test
  void createRequest_success() throws IOException {
    Long userId = 1L;
    RequestCreateRequestDto dto = RequestCreateRequestDto.builder()
        .prompt("test prompt").build();

    User user = User.builder().id(userId).build();

    List<String> stringKeywords = List.of("testKeyword1", "testKeyword2");

    RequestPromptRefineResult refineResult = RequestPromptRefineResult.builder()
        .title("test Title")
        .keywords(stringKeywords).build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(requestPromptAiService.refine(dto.getPrompt())).thenReturn(refineResult);
    when(objectMapper.writeValueAsString(stringKeywords))
        .thenReturn("[\"testKeyword1\",\"testKeyword2\"]");

    Keyword keyword1 = Keyword.builder().id(1L).rawText("testKeyword1").normalizedText("testKeyword1").build();
    Keyword keyword2 = Keyword.builder().id(2L).rawText("testKeyword2").normalizedText("testKeyword2").build();
    when(keywordService.createKeyword(any(KeywordCreateRequestDto.class)))
        .thenReturn(keyword1, keyword2);

    Track track1 = Track.builder().id(1L).build();
    Track track2 = Track.builder().id(2L).build();

    when(keywordTrackService.getTracksByKeyword(1L)).thenReturn(List.of(track1));
    when(keywordTrackService.getTracksByKeyword(2L)).thenReturn(List.of(track2));

    byte[] imageBytes=new byte[5];
    String thumbnailKey="thumbnailKey";
    String thumbnailUrl="thumbnailUrl";
    StoredFile stored = new StoredFile(thumbnailKey,thumbnailUrl);
    when(requestThumbnailAiService.generateThumbnail(
        dto.getPrompt(),
        refineResult.getTitle(),
        stringKeywords
    )).thenReturn(imageBytes);

    when(localFileStorageService.storeGeneratedThumbnail(any(Long.class),eq(imageBytes)))
        .thenReturn(stored);

    doAnswer(invocation -> {
      Request req = invocation.getArgument(0);
      req.setId(100L);
      return null;
    }).when(requestRepository).saveAndFlush(any(Request.class));

    ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);

    RequestResponseDto response=requestService.createRequest(dto, userId);

    verify(requestRepository).saveAndFlush(captor.capture());

    Request savedRequest = captor.getValue();



    assertThat(savedRequest.getUser()).isEqualTo(user);
    assertThat(savedRequest.getOriginalPrompt()).isEqualTo("test prompt");
    assertThat(savedRequest.getTitle()).isEqualTo("test Title");
    assertThat(savedRequest.getDeleted()).isFalse();
    assertThat(savedRequest.getThumbnailKey()).isEqualTo(thumbnailKey);
    assertThat(savedRequest.getThumbnailUrl()).isEqualTo(thumbnailUrl);

    verify(requestKeywordService).addKeywordByRequest(savedRequest, keyword1);
    verify(requestKeywordService).addKeywordByRequest(savedRequest, keyword2);
    verify(requestTrackService).addTrackByRequest(100L, track1.getId());
    verify(requestTrackService).addTrackByRequest(100L, track2.getId());



    assertThat(response.getTitle()).isEqualTo("test Title");
    assertThat(response.getThumbnailUrl()).isEqualTo(thumbnailUrl);
    assertThat(response.getKeywords()).isEqualTo(stringKeywords);

  }

  @Test
  void createRequest_whenUserNotFound_thenThrowException(){
    Long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> requestService.createRequest(any(RequestCreateRequestDto.class), userId))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");
  }
}
