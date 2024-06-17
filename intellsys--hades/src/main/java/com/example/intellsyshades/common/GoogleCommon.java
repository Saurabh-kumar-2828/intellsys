package com.example.intellsyshades.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class GoogleCommon {

  private final Environment environment;

  public GoogleCommon(Environment environment) {
    this.environment = environment;
  }

  public String getOAuthUrl(String callbackUrl, String state, String scopes){
    String clientId = environment.getProperty("GOOGLE_CLIENT_ID");

    return UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
        .queryParam("scope",scopes)
        .queryParam("client_id", clientId)
        .queryParam("response_type", "code")
        .queryParam("redirect_uri", callbackUrl)
        .queryParam("prompt", "consent")
        .queryParam("access_type", "offline")
        .queryParam("state", state)
        .build().toUriString();
  }

  public String getRefreshToken(String authorizationCode, String redirectUri) throws IOException {
    String clientId = environment.getProperty("GOOGLE_CLIENT_ID");
    String clientSecret = environment.getProperty("GOOGLE_CLIENT_SECRET");

    assert clientId != null;
    assert clientSecret != null;
    String requestBody = "code=" + URLEncoder.encode(authorizationCode, StandardCharsets.UTF_8) + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) + "&grant_type=authorization_code";

    HttpResponse<String> response;
    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://oauth2.googleapis.com/token"))
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    int responseCode = response.statusCode();
    String responseBody = response.body();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    if (responseCode == HttpURLConnection.HTTP_OK) {
      String refreshToken = jsonNode.get("refresh_token").asText();
      ObjectNode jsonResponse = objectMapper.createObjectNode();
      jsonResponse.put("refresh_token", refreshToken);
      return objectMapper.writeValueAsString(jsonResponse);
    } else {
      String errorMessage = jsonNode.get("error").asText();
      throw new IOException("Failed to exchange authorization code for refresh token. Error Message: " + errorMessage);
    }
  }

  public AccessToken getAccessToken(String refreshToken) throws IOException {
    String clientId = environment.getProperty("GOOGLE_CLIENT_ID");
    String clientSecret = environment.getProperty("GOOGLE_CLIENT_SECRET");
    assert clientId != null;
    assert clientSecret != null;
    UserCredentials userCredentials = UserCredentials.newBuilder().setClientId(clientId).setClientSecret(clientSecret).setRefreshToken(refreshToken).build();

    return userCredentials.refreshAccessToken();
    }
}
