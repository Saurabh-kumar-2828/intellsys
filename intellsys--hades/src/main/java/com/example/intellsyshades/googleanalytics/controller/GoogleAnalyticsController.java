package com.example.intellsyshades.googleanalytics.controller;

import com.example.intellsyshades.common.dto.GoogleAnalyticsAccount;
import com.example.intellsyshades.common.dto.GoogleRefreshTokenRequestDTO;
import com.example.intellsyshades.common.dto.RefreshTokenRequest;
import com.example.intellsyshades.googleanalytics.dto.AnalyticsProperty;
import com.example.intellsyshades.googleanalytics.service.GoogleAnalyticsService;
import com.google.api.services.analytics.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cc991d2b-dc83-458e-8e8d-9b47164c735f")
public class GoogleAnalyticsController {

  @Autowired
  private GoogleAnalyticsService googleAnalyticsService;

  // Main endpoint for Google Analytics
  @GetMapping("/")
  public String index() {
    return "This is the main Google Analytics endpoint";
  }

  // Endpoint to get Oauth Url
  @GetMapping("/oauth")
  public String oauth(@RequestParam("callbackUrl") String callbackUrl, @RequestParam("state") String state){
    return googleAnalyticsService.generateOAuthUrl(callbackUrl, state);
  }

  // Get Refresh Token from Authorization Code
  @PostMapping("/refresh-token")
  public ResponseEntity<String> refreshToken(@ModelAttribute GoogleRefreshTokenRequestDTO request) {
    String authorizationCode = request.getAuthorizationCode();
    String callbackUrl = request.getCallbackUrl();
    try {
      String refreshToken = googleAnalyticsService.getRefreshTokenFromAuthCode(authorizationCode, callbackUrl);
      return ResponseEntity.ok(refreshToken);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  // TODO: Create a route to get available accounts
  @PostMapping("/accessible-accounts")
  public ResponseEntity<List<AnalyticsProperty>> getAccessibleAccounts(@RequestBody RefreshTokenRequest refreshToken) {
    try{
      List<AnalyticsProperty> properties = googleAnalyticsService.getProperties(refreshToken.getRefreshToken());
      return new ResponseEntity<>(properties, HttpStatus.OK);
    } catch (Exception e){
      e.printStackTrace();
      return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/initial-load")
  public ResponseEntity<String> initialDataLoad(@RequestBody Map<String, Object> requestData){
    try{
      UUID dataSourceId = UUID.fromString((String) requestData.get("dataSourceId"));
      String startDate = (String) requestData.get("startDate");
      String endDate = (String) requestData.get("endDate");
     googleAnalyticsService.initialLoad(dataSourceId, startDate, endDate);
     return new ResponseEntity<>("Initialization completed", HttpStatus.OK);
    }catch (IllegalArgumentException e) {
      return new ResponseEntity<>("Invalid UUID format", HttpStatus.BAD_REQUEST);
    } catch (RuntimeException e){
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
