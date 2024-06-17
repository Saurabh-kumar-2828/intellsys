package com.example.intellsyshades.googleads.dto;

public class GoogleAdsCredentials {
  private String refreshToken;
  private String accessToken;
  private String accountId;
  private String customerAccountId;

  public String getRefreshToken() {
    return refreshToken;
  }
  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getAccessToken() {
    return accessToken;
  }
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAccountId() {
    return accountId;
  }
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getCustomerAccountId() {
    return customerAccountId;
  }
  public void setCustomerAccountId(String customerAccountId) {
    this.customerAccountId = customerAccountId;
  }
}
