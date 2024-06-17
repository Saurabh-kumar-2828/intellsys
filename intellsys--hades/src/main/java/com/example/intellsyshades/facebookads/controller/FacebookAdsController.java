package com.example.intellsyshades.facebookads.controller;

import com.example.intellsyshades.facebookads.service.FacebookAdsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/d80731db-155e-4a24-bc58-158a57edabd7")
public class FacebookAdsController {

  @Autowired
  private FacebookAdsService facebookAdsService;

  // Main endpoint for FacebookAds
  @GetMapping("/")
  public String index(){
    return "This is the main FacebookAds endpoint";
  }

  @GetMapping("/oauth")
  public String oauth(@RequestParam("callbackUrl") String callbackUrl, @RequestParam("state") String state){
    return facebookAdsService.generateOAuthUrl(callbackUrl,state);
  }

  // TODO: Create Route to get Refresh Token from Authorization Code.

  // TODO: Create a route to get available accounts
}
