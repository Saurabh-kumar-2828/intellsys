package com.example.intellsyshades.googleads.controller;

import com.example.intellsyshades.common.GoogleCommon;
import com.example.intellsyshades.common.dto.AccessibleAccountDTO;
import com.example.intellsyshades.common.dto.GoogleRefreshTokenRequestDTO;
import com.example.intellsyshades.googleads.service.GoogleAdsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/800c28ce-43ea-44b8-b6fc-077f44566296")
public class GoogleAdsController {

    @Autowired
    private GoogleAdsService googleAdsService;
    private GoogleCommon googleCommon;

    // Main endpoint for Google Ads
    @GetMapping("/")
    public String index() {
        return "Handling Google Ads request";
    }

    // Endpoint to get Oauth Url
    @GetMapping("/oauth")
    public ResponseEntity<String> oauth(@RequestParam("callbackUrl") String callbackUrl, @RequestParam("state") String state) {
        try{

        return ResponseEntity.ok(googleAdsService.generateOAuthUrl(callbackUrl,state));
        }catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Get Refresh Token from Authorization Code
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@ModelAttribute GoogleRefreshTokenRequestDTO request) {
        String authorizationCode = request.getAuthorizationCode();
        String callbackUrl = request.getCallbackUrl();
        try {
            String refreshToken = googleAdsService.getRefreshTokenFromAuthCode(authorizationCode, callbackUrl);
            return ResponseEntity.ok(refreshToken);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // TODO: Fix account listing.
    @PostMapping("/list-accessible-accounts")
    public ResponseEntity<List<AccessibleAccountDTO>> accessibleAccounts(@RequestParam String refreshToken){
//        List<AccessibleAccountDTO> accounts;
        return ResponseEntity.status(HttpStatus.OK).body(null);

    }

    @PostMapping("/initial-load")
    public ResponseEntity<String> initialLoad(@RequestBody Map<String, Object> requestData){
        try{
            UUID dataSourceId = UUID.fromString((String) requestData.get("dataSourceId"));
            String startDate = (String) requestData.get("startDate");
            String endDate = (String) requestData.get("endDate");
            googleAdsService.initialLoad(dataSourceId, startDate,endDate);
            return new ResponseEntity<>("Initialization completed", HttpStatus.OK);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
