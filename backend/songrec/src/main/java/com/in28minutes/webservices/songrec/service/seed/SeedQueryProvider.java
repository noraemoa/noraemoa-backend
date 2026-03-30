package com.in28minutes.webservices.songrec.service.seed;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SeedQueryProvider {
  public List<String> getSeedQueries(){
    return List.of(
        "백예린",
        "AKMU",
        "DEAN",
        "EXO",
        "잔나비",
        "검정치마",
        "DAY6",
        "IVE",
        "KiiiKiii",
        "BTS",
        "WOODZ",
        "화사",
        "ITZY",
        "BLACKPINK",
        "카더가든",
        "NMIXX",
        "도경수",
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
        "우효"
    );
  }
}
