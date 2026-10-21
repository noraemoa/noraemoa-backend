package com.noraemoa.songrec.global.exception;

/**
 * 추천에 꼭 필요한 외부 API(OpenAI, Qdrant) 호출이 실패했을 때 쓰는 예외. 503 으로 응답한다.
 */
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
