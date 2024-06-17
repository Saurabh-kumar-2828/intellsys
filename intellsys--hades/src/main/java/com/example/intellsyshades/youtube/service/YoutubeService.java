package com.example.intellsyshades.youtube.service;

import com.example.intellsyshades.common.GoogleCommon;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class YoutubeService {

  private final GoogleCommon googleCommon;
  public YoutubeService(final GoogleCommon googleCommon) {
    this.googleCommon = googleCommon;
  }

  public String generateOAuthUrl(String callbackUrl, String state){
    String scopes = "https://www.googleapis.com/auth/youtube.readonly";
    return googleCommon.getOAuthUrl(callbackUrl, state, scopes);
  }

  public String getRefreshTokenFromAuthCode(String authorizationCode,String callbackUrl){
    try {
      return googleCommon.getRefreshToken(authorizationCode, callbackUrl);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
