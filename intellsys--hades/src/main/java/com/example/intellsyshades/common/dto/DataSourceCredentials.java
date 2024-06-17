package com.example.intellsyshades.common.dto;

import java.util.UUID;

public class DataSourceCredentials {
  private String sourceCredentials;
  private UUID companyId;
  private String accountId;
  private String databaseCredentials;

  public DataSourceCredentials(String sourceCredentials, UUID companyId, String accountId, String databaseCredentials) {
    this.sourceCredentials = sourceCredentials;
    this.companyId = companyId;
    this.accountId = accountId;
    this.databaseCredentials = databaseCredentials;
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

  public String getDatabaseCredentials() {
    return databaseCredentials;
  }
  public void setDatabaseCredentials(String databaseCredentials) {
    this.databaseCredentials = databaseCredentials;
  }
}
