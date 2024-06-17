package com.example.intellsyshades.googleanalytics.dto;

import com.google.auth.oauth2.AccessToken;

public class GoogleAnalyticsCredentials {
  private String refreshToken;
  private String propertyId;
  private String accessToken; // Optional

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getPropertyId() {
    return propertyId;
  }

  public void setPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
