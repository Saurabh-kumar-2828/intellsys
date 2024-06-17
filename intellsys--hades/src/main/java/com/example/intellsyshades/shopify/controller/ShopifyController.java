package com.example.intellsyshades.shopify.controller;

import com.example.intellsyshades.shopify.service.ShopifyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/b84a3317-7296-425a-9367-02b7dd7c3116")
public class ShopifyController {

  private final ShopifyService shopifyService;

  public ShopifyController(ShopifyService shopifyService) {
    this.shopifyService = shopifyService;
  }

  // Main endpoint for Shopify
  @GetMapping("/")
  public String index() {
    return "This is the main Shopify endpoint";
  }

  @GetMapping("/oauth")
  public String oauth(@RequestParam("callbackUrl") String callbackUrl, @RequestParam("state") String state){
    return shopifyService.generateOAuthUrl(callbackUrl, state);
  }
}
