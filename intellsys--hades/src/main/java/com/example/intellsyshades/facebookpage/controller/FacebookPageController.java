package com.example.intellsyshades.facebookpage.controller;

import com.example.intellsyshades.facebookpage.service.FacebookPageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/a2c4d7a3-17b4-4761-80cf-1b9e5634e293")
public class FacebookPageController {

  private final FacebookPageService facebookPageService;

  public FacebookPageController(FacebookPageService facebookPageService) {
    this.facebookPageService = facebookPageService;
  }

  // Main endpoint for FacebookPage
  @GetMapping("/")
  public String index() {
    return " This is the main FacebookPage endpoint";
  }

  @GetMapping("/oauth")
  public String oauth(@RequestParam("callbackUrl") String callbackUrl, @RequestParam("state") String state){
    return facebookPageService.generateOAuthUrl(callbackUrl, state);
  }

  // TODO: Create Route to get Refresh Token from Authorization Code.

  // TODO: Create a route to get available accounts
}
