package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.User;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.RequestRepository;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {
    private static final Logger log = LoggerFactory.getLogger(RequestService.class);
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
        Request request = getRequestByUserIdAndRequestId(userId,requestId);
        request.setTitle(requestDto.getTitle());

        return request;
    }

    @Transactional(readOnly = true)
    public Request getRequestByUserIdAndRequestId(Long userId, Long requestId){
        if(!userService.existUser(userId)) throw new NotFoundException("해당 사용자를 찾을 수 없습니다.");

        return requestRepository.findByIdAndUserId(requestId,userId)
                .orElseThrow(()->new NotFoundException("해당 요청을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByUserId(Long userId) {
        if(!userService.existUser(userId)) throw new NotFoundException("해당 사용자를 찾을 수 없습니다.");
        return requestRepository.findAllByUserId(userId);
    }

    public Request getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(()->new NotFoundException("해당 요청을 찾을 수 없습니다."));
    }
}
