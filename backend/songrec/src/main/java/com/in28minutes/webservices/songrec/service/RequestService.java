package com.in28minutes.webservices.songrec.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.request.RequestResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.integration.openai.dto.RequestPromptRefineResult;
import com.in28minutes.webservices.songrec.repository.RequestRepository;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final LocalFileStorageService localFileStorageService;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final RequestPromptAiService requestPromptAiService;
    private final RequestThumbnailAiService requestThumbnailAiService;

    @Transactional
    public Request updateRequest(RequestCreateRequestDto requestDto,Long userId, Long requestId){
        Request request = getActiveRequest(userId,requestId);
        request.setTitle(requestDto.getPrompt());
        return request;
    }

    @Transactional(readOnly = true)
    public Request getActiveRequest(Long userId, Long requestId){
        return requestRepository.findByIdAndUserIdAndDeletedFalse(requestId,userId)
                .orElseThrow(()->new NotFoundException("해당 요청을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Request getRequestFeed( Long requestId){
        return requestRepository.findByIdAndDeletedFalse(requestId)
                .orElseThrow(()->new NotFoundException("해당 요청을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<Request> getAllRequests(int pageNumber,int pageSize){
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        return requestRepository.findFeed(pageable);
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByUserId(Long userId) {
        return requestRepository.findAllByUserIdAndDeletedFalse(userId);
    }

    @Transactional
    public void deleteRequest(Long userId, Long requestId) {
        Request request = getActiveRequest(userId,requestId);
        request.setDeleted(true);
    }

    @Transactional
    public void deleteRequestAdmin( Long requestId) {
        Request request = getRequestFeed(requestId);
        request.setDeleted(true);
    }

    @Transactional
    public Request uploadThumbnail(Long userId, Long requestId, MultipartFile file) throws IOException {
        Request request = getActiveRequest(userId,requestId);

        LocalFileStorageService.StoredFile stored =
                localFileStorageService.storeRequestThumbnail(requestId,file);

        request.setThumbnailKey(stored.key());
        request.setThumbnailUrl(stored.url());
        return request;
    }

    private String writeKeywords(List<String> keywords){
        try{
            return objectMapper.writeValueAsString(keywords);
        }catch (JsonProcessingException e){
            throw new RuntimeException("Failed to serialize keywords",e);
        }
    }

    private String fallbackTitle(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "My Playlist";
        }

        String normalized = prompt.trim();

        if (normalized.length() <= 30) {
            return normalized;
        }

        return normalized.substring(0, 30);
    }

    public List<String> readKeywords(String keywordsJson){
        try{
            return objectMapper.readValue(
                keywordsJson,
                new TypeReference<List<String>>() {}
            );
        }catch (Exception e){
            return List.of();
        }
    }

    @Transactional
    public RequestResponseDto createRequest(RequestCreateRequestDto dto,Long userId ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        RequestPromptRefineResult refineResult;
        try {
            refineResult = requestPromptAiService.refine(dto.getPrompt());
        } catch (Exception e) {
            e.printStackTrace();
            refineResult = new RequestPromptRefineResult(
                fallbackTitle(dto.getPrompt()),
                List.of()
            );
        }

        Request request = Request.builder()
            .user(user)
            .originalPrompt(dto.getPrompt())
            .title(refineResult.getTitle())
            .promptKeywordsJson(writeKeywords(refineResult.getKeywords()))
            .deleted(false)
            .build();

        requestRepository.save(request);

        try{
            byte[] imageBytes = requestThumbnailAiService.generateThumbnail(
                dto.getPrompt(),
                refineResult.getTitle(),
                refineResult.getKeywords()
            );
            LocalFileStorageService.StoredFile stored=
                localFileStorageService.storeGeneratedThumbnail(request.getId(),imageBytes);

            request.setThumbnailKey(stored.key());
            request.setThumbnailUrl(stored.url());
        }catch (Exception e){
            System.out.println("Thumbnail generation failed");
            e.printStackTrace();
        }

        return RequestResponseDto.from(request, refineResult.getKeywords());
    }
}
