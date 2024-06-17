package com.example.intellsyshades.shopify.service;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ShopifyService {

  private final Environment environment;

  public ShopifyService(Environment environment) {
    this.environment = environment;
  }

  public String generateOAuthUrl(String callbackUrl, String state){
    String clientId = environment.getProperty("SHOPIFY_CLIENT_ID");

    return UriComponentsBuilder.fromHttpUrl("https://shopify.com/admin/oauth/authorize")
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", callbackUrl)
        .queryParam("scope", "shop")
        .queryParam("response_type","code")
        .queryParam("prompt", "select_account")
        .queryParam("state", state)
        .build().toUriString();
  }
}
