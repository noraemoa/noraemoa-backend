package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;

    public Request createRequest(Request request) {
        return requestRepository.save(request);
    }

    public List<Request> getRequestsByUserId(Long user_id) {
        return requestRepository.findAllByUserId(user_id);
    }

    public Request getRequestById(Long id) {
        return requestRepository.findById(id).orElse(null);
    }
}
