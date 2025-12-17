package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.User;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.KeywordResponseDto;
import com.in28minutes.webservices.songrec.dto.response.RequestResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;

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
    public List<Request> getRequestsByUserId(Long userId) {
        return requestRepository.findAllByUserIdAndDeletedFalse(userId);
    }

    @Transactional
    public void deleteRequest(Long userId, Long requestId) {
        Request request = getActiveRequest(userId,requestId);
        request.setDeleted(true);
    }
}
