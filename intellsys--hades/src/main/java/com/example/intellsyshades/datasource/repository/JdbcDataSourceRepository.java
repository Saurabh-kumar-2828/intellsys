package com.example.intellsyshades.datasource.repository;

import com.example.intellsyshades.IntegrationType;
import com.example.intellsyshades.common.dto.DataSourceCredentials;
import com.example.intellsyshades.common.dto.DataSourceDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class JdbcDataSourceRepository implements DataSourceRepository{

  private final DataSource dataSource;
  private final ObjectMapper objectMapper;

  public JdbcDataSourceRepository(DataSource dataSource, ObjectMapper objectMapper){
    this.dataSource = dataSource;
    this.objectMapper = objectMapper;
  }

  /**
   *
   * @param dataSourceDto
   */
  @Override
  public void insertDataSource(DataSourceDTO dataSourceDto){
    String sql = "INSERT INTO data_sources (id, created_at, updated_at, integration_type, source_credentials, company_id, account_id, account_details, user_id) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, cast(? as json), ?)";

    Timestamp timestamp = Timestamp.from(Instant.now());
    try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)){
      preparedStatement.setObject(1, dataSourceDto.getId());
      preparedStatement.setTimestamp(2, timestamp);
      preparedStatement.setTimestamp(3, timestamp);
      preparedStatement.setObject(4, dataSourceDto.getIntegrationType());
      preparedStatement.setString(5, dataSourceDto.getSourceCredentials());
      preparedStatement.setObject(6, dataSourceDto.getCompanyId());
      preparedStatement.setObject(7, dataSourceDto.getAccountId());
      // Convert extraInformation to JSON string
      String AccountDetailsJson = objectMapper.writeValueAsString(dataSourceDto.getAccountDetails());
      preparedStatement.setString(8, AccountDetailsJson);
      preparedStatement.setObject(9, dataSourceDto.getUserId());
      preparedStatement.executeUpdate();
    } catch (SQLException | JsonProcessingException e) {
      throw new RuntimeException("Error inserting data source: " + e.getMessage(), e);
    }
  }

  /**
   *
   * @param id data source id
   */
  @Override
  public void deleteDataSourceById(UUID id) {
    String sql = "DELETE FROM data_sources WHERE id = ?";
    try(Connection connection = dataSource.getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement(sql)){
      preparedStatement.setObject(1,id);
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error deleting data source: " + e.getMessage(), e);
    }
  }

  /**
   *
   * @param dataSourceDTO datasource details. - Id, IntegrationType
   * @param subscriptionTier subscription level mapped to the cursor
   */
  @Override
  public void insertSubDataSource(DataSourceDTO dataSourceDTO, Integer subscriptionTier){
    String sql = "INSERT INTO data_sources_metadata (data_source_id, data_source_table_type, updated_at, historical_cursor, future_cursor, historical_cursor_threshold, comments, subscription_tier)" +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    Timestamp timestamp = Timestamp.from(Instant.now());
    try(Connection connection = dataSource.getConnection();
      PreparedStatement preparedStatement = connection.prepareStatement(sql)){
      preparedStatement.setObject(1,dataSourceDTO.getId());
      preparedStatement.setObject(2,dataSourceDTO.getIntegrationType());
      preparedStatement.setTimestamp(3, timestamp);
      preparedStatement.setTimestamp(4, timestamp);
      preparedStatement.setTimestamp(5, timestamp);

      for(int tier =0; tier < subscriptionTier; tier++){
        preparedStatement.setTimestamp(6, Timestamp.from(Instant.now().plus(Duration.ofDays(IntegrationType.getThresholdDays(dataSourceDTO.getIntegrationType())))));
        preparedStatement.setObject(7, IntegrationType.getName(dataSourceDTO.getIntegrationType()));
        preparedStatement.setObject(8, subscriptionTier);
        preparedStatement.addBatch();
      }
      preparedStatement.executeBatch();
    }catch(SQLException e){
      throw new RuntimeException("Error inserting sub data source: " + e.getMessage(), e);
    }
  }

  /**
   *
   * @param userId user id
   * @param dataSourceId data source id
   */
  @Override
  public void assignDataSourcePermissionsToCreator(UUID userId, UUID dataSourceId){
    String sql = "INSERT INTO user_to_data_source_permissions (user_id, data_source_id, can_read, can_edit, can_delete, can_share, can_enumerate)" +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)){
        preparedStatement.setObject(1, userId);
        preparedStatement.setObject(2, dataSourceId);
        preparedStatement.setObject(3, true);
        preparedStatement.setObject(4, true);
        preparedStatement.setObject(5, true);
        preparedStatement.setObject(6, true);
        preparedStatement.setObject(7, true);
        preparedStatement.executeUpdate();
    }catch(SQLException e){
      throw new RuntimeException("Error assigning permissions to data source: " + e.getMessage(), e);
    }
  }

  /**
   *
   * @param companyId company id
   * @param dataSourceId data source id
   */
  @Override
  public void mapCompanyToDataSource(UUID companyId, UUID dataSourceId){
    String sql = "INSERT INTO company_to_data_source_permissions (company_id, data_source_id, can_read, can_edit, can_delete, can_share, can_enumerate)" +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    try(Connection connection = dataSource.getConnection();
      PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
      preparedStatement.setObject(1,companyId);
      preparedStatement.setObject(2, dataSourceId);
      preparedStatement.setObject(3, false);
      preparedStatement.setObject(4, false);
      preparedStatement.setObject(5, false);
      preparedStatement.setObject(6, false);
      preparedStatement.setObject(7, false);
      preparedStatement.executeUpdate();
    }catch(SQLException e){
      throw new RuntimeException("Error inserting data source: " + e.getMessage(), e);
    }
  }

  /**
   *
   * @param dataSourceDetails
   * @param storageTableIds List of table Ids.
   */
  @Override
  public void insertStorageTableIds(DataSourceDTO dataSourceDetails, Map<UUID, String> storageTableIds){
    String sql = "INSERT INTO storage_table_id (id, integration_type, account_id, table_name, datasource_id)"+
        "VALUES (?, ?, ?, ?, ?)";
    try(Connection connection = dataSource.getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement(sql)){
      for (Map.Entry<UUID, String> entry : storageTableIds.entrySet()) {
        preparedStatement.setObject(1, entry.getKey());
        preparedStatement.setObject(2, dataSourceDetails.getIntegrationType());
        preparedStatement.setString(3, dataSourceDetails.getAccountId());
        preparedStatement.setString(4, entry.getValue());
        preparedStatement.setString(5, dataSourceDetails.getId().toString());
        preparedStatement.addBatch();
      }
      preparedStatement.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException("Error inserting storage table ids: " + e);
    }
  }

  /**
   *
   * @param dataSourceId Data source id
   * @return
   */
  public DataSourceCredentials getDataSourceCredentialsDetailsById(UUID dataSourceId){
    String sql = "SELECT data_sources.source_credentials, data_sources.company_id, data_sources.account_id, " +
        "companies.database_credentials " +
        "FROM data_sources " +
        "JOIN companies ON data_sources.company_id = companies.id " +
        "WHERE data_sources.id = ?";
    try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)){
        preparedStatement.setObject(1, dataSourceId);
        try(ResultSet resultSet = preparedStatement.executeQuery()){
          if(resultSet.next()){
            String sourceCredentials = resultSet.getString("source_credentials");
            UUID companyId = UUID.fromString(resultSet.getString("company_id"));
            String accountId = resultSet.getString("account_id");
            String databaseCredentials = resultSet.getString("database_credentials");
            return new DataSourceCredentials(sourceCredentials, companyId, accountId, databaseCredentials);
          } else {
            throw new RuntimeException("Error getting data source details: " + dataSourceId);
          }
        }
    } catch(SQLException e){
      throw new RuntimeException("Error getting data source details: " + e);
    }
  }

  /**
   *
   * @param dataSourceId Data source id
   * @return List of all cursors for a data source.
   */
  public List<UUID> getAllCursorsForDataSource(UUID dataSourceId){
    String sql = "SELECT id FROM data_sources_metadata" +
        " WHERE data_source_id = ?";
    List<UUID> cursors = new ArrayList<>();
    try(Connection connection = dataSource.getConnection();
      PreparedStatement preparedStatement = connection.prepareStatement(sql)){
      preparedStatement.setObject(1,dataSourceId);
      try(ResultSet resultSet = preparedStatement.executeQuery()){
        while(resultSet.next()){
          cursors.add(UUID.fromString(resultSet.getString("id")));
        }
      }
    } catch(SQLException e){
      throw new RuntimeException("Error getting cursors: " + e);
    }
    return cursors;
  }

  /**
   *
   * @param cursorId datasource metadata id.
   * @return subscription tier
   */
  public Integer getSubscriptionTierFromCursorId(UUID cursorId){
/*
    String sql = "SELECT companies.subscription_tier "+
        "FROM data_sources_metadata " +
        "JOIN data_sources ON data_sources_metadata.data_source_id = data_sources.id " +
        "JOIN companies ON data_sources.company_id = companies.id " +
        "WHERE data_sources_metadata.id = ?";
*/
    String sql = "SELECT subscription_tier FROM data_sources_metadata"+
        " WHERE id = ?";
    try(Connection connection = dataSource.getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement(sql)){
      preparedStatement.setObject(1, cursorId);
      try(ResultSet resultSet = preparedStatement.executeQuery()){
        if(resultSet.next()){
          return resultSet.getInt(1);
        } else {
          throw new IllegalArgumentException("Error getting subscription tier: " + cursorId);
        }
      }
    } catch (SQLException e){
      throw new RuntimeException("Error getting subscription tier: " + cursorId);
    }
  }

  /**
   * A function which updates cursor dates
   * @param cursorId
   * @param startDate
   */
  public void updateCursor(UUID cursorId, String startDate){
    // TODO: Finish this.
  }
}
