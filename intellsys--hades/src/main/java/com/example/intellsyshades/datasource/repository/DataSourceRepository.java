package com.example.intellsyshades.datasource.repository;

import com.example.intellsyshades.common.dto.DataSourceCredentials;
import com.example.intellsyshades.common.dto.DataSourceDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DataSourceRepository {

  void insertDataSource(DataSourceDTO dataSource);

  void deleteDataSourceById(UUID id);

  void insertSubDataSource(DataSourceDTO dataSource, Integer subscriptionTier);

  void assignDataSourcePermissionsToCreator(UUID userId, UUID dataSourceId);

  void mapCompanyToDataSource(UUID companyId, UUID dataSourceId);

  void insertStorageTableIds(DataSourceDTO dataSourceId, Map<UUID, String> storageTableIds);

  DataSourceCredentials getDataSourceCredentialsDetailsById(UUID dataSourceId);

  List<UUID> getAllCursorsForDataSource(UUID dataSourceId);

  Integer getSubscriptionTierFromCursorId(UUID cursorId);

  void updateCursor(UUID cursorId, String startDate);
}
