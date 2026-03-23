package com.in28minutes.webservices.songrec.global.util;

public class TextNormalizer {
  private TextNormalizer(){}

  public static String normalize(String input){
    if(input==null) return null;
    return input
        .trim()
        .toLowerCase()
        .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsHangul}]", "")
        .replaceAll("\\s+", "");
  }
}
