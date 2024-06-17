package com.example.intellsyshades.datasource.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CompanyRepository {
  Integer getSubscriptionTier(UUID companyId);
  Map<UUID, String> createTables(Map<String, List<Map<String, String>>> tableDetails, String databaseCredentials);
  String getCompanyDbCredentials(UUID companyId);
  void insertDataIntoCompanyDB(List<Map<String, String>> dataList, String databaseCredentials, String tableName);
}
