package com.example.intellsyshades.common.dto;

import com.example.intellsyshades.IntegrationType;

import java.sql.Timestamp;
import java.util.UUID;

public class DataSourceDTO {
  private UUID id;
  private UUID integrationType;
  private String sourceCredentials;
  private UUID companyId;
  private String accountId;
  private Object accountDetails;
  private UUID userId;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getIntegrationType() {
    return integrationType;
  }

  public void setIntegrationType(UUID integrationType) {
    if (!IntegrationType.isValid(integrationType)) {
      throw new IllegalArgumentException("Invalid integration type UUID: " + integrationType);
    }
    this.integrationType = integrationType;
  }

  public String getSourceCredentials() {
    return sourceCredentials;
  }

  public void setSourceCredentials(String sourceCredentials) {
    this.sourceCredentials = sourceCredentials;
  }

  public UUID getCompanyId() {
    return companyId;
  }

  public void setCompanyId(UUID companyId) {
    this.companyId = companyId;
  }

  public String getAccountId() {
    return accountId;
  }
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }
  public Object getAccountDetails() {
    return accountDetails;
  }

  public void setAccountDetails(Object accountDetails) {
    this.accountDetails = accountDetails;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }
}
