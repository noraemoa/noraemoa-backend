package com.in28minutes.webservices.songrec.service.seed;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SeedQueryProvider {
  public List<String> getSeedQueries(){
    return List.of(
        "태연",
        "소녀시대",
        "Hearts2Hearts",
        "김하온",
        "Crush",
        "GRAY",
        "aespa",
        "이준형",
        "비투비",
        "유다빈밴드",
        "김필선",
        "김결",
        "백아",
        "선우정아",
        "유라",
        "조이",
        "SOLE",
        "CHEEZE",
        "BIG Naughty",
        "케이시",
        "죠지"
    );
  }
}
