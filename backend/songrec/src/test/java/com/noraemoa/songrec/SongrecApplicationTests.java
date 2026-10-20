package com.noraemoa.songrec;

import com.noraemoa.songrec.config.QdrantInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SongrecApplicationTests {

    // 기동 시 Qdrant 에 컬렉션 생성 요청을 보내므로, 외부 서버 없이 컨텍스트만 검증하도록 대체한다.
    @MockBean
    private QdrantInitializer qdrantInitializer;

    @Test
    void contextLoads() {
    }

}
