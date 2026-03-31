package com.in28minutes.webservices.songrec.service.seed;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SeedQueryProvider {
  public List<String> getSeedQueries(){
    return List.of(

        "다비치",
        "10cm",
        "이무진",
        "ALLDAY PROJECT",
        "오존",
        "멜로망스",
        "아이들",
        "루시",
        "볼빨간사춘기",
        "TOUCHED",
        "박소은",
        "우효",
        "프롬",
        "너드커넥션",
        "김수영",
        "로꼬",
        "비비",
        "오반",
        "최유리",
        "스텔라장",
        "싸이",
        "정오월",
        "혁오",
        "알레프"
    );
  }
}
