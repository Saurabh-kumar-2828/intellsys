package com.example.intellsyshades.instagram.controller;

import com.example.intellsyshades.instagram.service.InstagramService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/c1e7b3f9-f613-491d-b217-029dab6eca26")
public class InstagramController {

  private final InstagramService instagramService;

  public InstagramController(InstagramService instagramService){
    this.instagramService = instagramService;
  }

  // Main endpoint for Instagram
  @GetMapping("/")
  public String index(){
    return "This is the main Instagram endpoint";
  }

  @GetMapping("/oauth")
  public String oauth(@RequestParam("callbackUrl") String callbackUrl, @RequestParam("state") String state){
    return instagramService.generateOAuthUrl(callbackUrl, state);
  }

  // TODO: Create Route to get Refresh Token from Authorization Code.

  // TODO: Create a route to get available accounts
}
