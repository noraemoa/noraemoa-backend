package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.RequestRepository;
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
    private final UserService userService;
    private final LocalFileStorageService localFileStorageService;

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Transactional
    public Request createRequest(RequestCreateRequestDto requestDto,Long userId) {
        User user = userService.getUserById(userId);
        Request request = Request.builder()
                .user(user)
                .deleted(false)
                .title(requestDto.getTitle())
                .build();
        return requestRepository.save(request);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Transactional
    public Request updateRequest(RequestCreateRequestDto requestDto,Long userId, Long requestId){
        Request request = getActiveRequest(userId,requestId);
        request.setTitle(requestDto.getTitle());
        return request;
    }

    @Transactional(readOnly = true)
    public Request getActiveRequest(Long userId, Long requestId){
        return requestRepository.findByIdAndUserIdAndDeletedFalse(requestId,userId)
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

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Transactional
    public void deleteRequest(Long userId, Long requestId) {
        Request request = getActiveRequest(userId,requestId);
        request.setDeleted(true);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    @Transactional
    public Request uploadThumbnail(Long userId, Long requestId, MultipartFile file) throws IOException {
        Request request = getActiveRequest(userId,requestId);

        LocalFileStorageService.StoredFile stored =
                localFileStorageService.storeRequestThumbnail(requestId,file);

        request.setThumbnailKey(stored.key());
        request.setThumbnailUrl(stored.url());
        return request;
    }
}
