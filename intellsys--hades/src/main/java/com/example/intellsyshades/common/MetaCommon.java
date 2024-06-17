package com.example.intellsyshades.common;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MetaCommon {

  private final Environment environment;

  public MetaCommon(Environment environment) {
    this.environment = environment;
  }

  public String getOAuthUrl(String callbackUrl, String state, String scopes){
    String clientId = environment.getProperty("FACEBOOK_CLIENT_ID");
    String apiVersion = environment.getProperty("FACEBOOK_API_VERSION");
    return UriComponentsBuilder.fromHttpUrl("https://www.facebook.com")
        .path(apiVersion+ "/dialog/oauth")
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", callbackUrl)
        .queryParam("scope", scopes)
        .queryParam("state", state)
        .build().toUriString();
  }
}
