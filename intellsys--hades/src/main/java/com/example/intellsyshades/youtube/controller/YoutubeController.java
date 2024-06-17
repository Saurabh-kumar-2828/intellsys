package com.example.intellsyshades.youtube.controller;

import com.example.intellsyshades.common.dto.GoogleRefreshTokenRequestDTO;
import com.example.intellsyshades.youtube.service.YoutubeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/3bcd4c2a-45d8-49c3-b226-25433a6e783e")
public class YoutubeController {

  @Autowired
  private YoutubeService youtubeService;

  // Main endpoint for Youtube
  @GetMapping("/")
  public String index(){
    return "This is the main Youtube endpoint";
  }

  @GetMapping("/oauth")
  public String oauth(@RequestParam("callbackUrl") String callbackUrl, @RequestParam("state") String state){
    return youtubeService.generateOAuthUrl(callbackUrl, state);
  }

  // Get Refresh Token from Authorization Code
  @PostMapping("/refresh-token")
  public ResponseEntity<String> refreshToken(@ModelAttribute GoogleRefreshTokenRequestDTO request) {
    String authorizationCode = request.getAuthorizationCode();
    String callbackUrl = request.getCallbackUrl();
    try {
      String refreshToken = youtubeService.getRefreshTokenFromAuthCode(authorizationCode, callbackUrl);
      return ResponseEntity.ok(refreshToken);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  // TODO: Create a route to get available accounts
}
