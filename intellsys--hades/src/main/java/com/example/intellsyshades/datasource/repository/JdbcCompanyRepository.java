package com.example.intellsyshades.datasource.repository;

import com.example.intellsyshades.service.PostgresManager;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;

@Repository
public class JdbcCompanyRepository implements CompanyRepository{

  private final DataSource dataSource;
  private final PostgresManager postgresManager;
  public JdbcCompanyRepository(DataSource dataSource, PostgresManager postgresManager) {
    this.dataSource = dataSource;
    this.postgresManager = postgresManager;
  }

  /**
   *
   * @param companyId company id
   * @return subscription tier for the company
   */
  @Override
  public Integer getSubscriptionTier(UUID companyId){
    String sql = "SELECT subscription_tier FROM companies WHERE id = ?";
    try(Connection connection = dataSource.getConnection();
    PreparedStatement preparedStatement = connection.prepareStatement(sql)){
      preparedStatement.setObject(1, companyId);
      try(ResultSet resultSet = preparedStatement.executeQuery()){
        if(resultSet.next()){
          return resultSet.getInt(1);
        } else {
          throw new IllegalArgumentException("No subscription tier found for company with ID " + companyId);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getCompanyDbCredentials(UUID companyId) {
    String sql = "SELECT database_credentials FROM companies WHERE id = ?";
    try(Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)){
      preparedStatement.setObject(1, companyId);
      try(ResultSet resultSet = preparedStatement.executeQuery()){
        if(resultSet.next()){
          return resultSet.getString(1);
        } else {
          throw new IllegalArgumentException("No database credentials found for company with ID " + companyId);
        }
      }
    } catch (SQLException e){
      throw new RuntimeException(e);
    }
  }

  /**
   *
   * @param tableDetails [tableName: {"columnName: "", "columnType": ""}]
   * @param databaseCredentials Credentials to configure postgres for that company.
   * @Description Creates tables dynamically based on columnName and Type passed.
   * @return Map of UUID of table and tableName
   */
  @Override
  public Map<UUID, String> createTables(Map<String, List<Map<String, String>>> tableDetails, String databaseCredentials) {
    Map<UUID, String> result = new HashMap<>();
    for(Map.Entry<String, List<Map<String, String>>> entry : tableDetails.entrySet()){
      String tableName = entry.getKey();
      List<Map<String, String>> columns = entry.getValue();
      StringBuilder queryBuilder = new StringBuilder();
      UUID tableUUID = UUID.randomUUID();
      result.put(tableUUID, tableName);
      queryBuilder.append("CREATE TABLE ").append("\"").append(tableUUID.toString()).append("\"").append(" (");
      for(Map<String, String> column : columns){
        String columnName = column.get("columnName");
        String columnType = column.get("columnType");
        queryBuilder.append(columnName).append(" ").append(columnType).append(",");
      }
      queryBuilder.deleteCharAt(queryBuilder.length() - 1);
      queryBuilder.append(")");

      String createTableQuery = queryBuilder.toString();
      System.out.println("Table Query: "+createTableQuery);
      try(Connection connection = postgresManager.getConnection(databaseCredentials);
          Statement statement = connection.createStatement()){
        statement.executeUpdate(createTableQuery);
      }catch(Exception e){
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  /**
   *
   * @param dataList Data from SDK/API to be inserted
   * @param databaseCredentials Credentials to configure Postgres Driver
   * @param tableName Name of the table
   */
  @Override
  public void insertDataIntoCompanyDB(List<Map<String, String>> dataList, String databaseCredentials, String tableName){
    StringBuilder columns = new StringBuilder();
    StringBuilder valuesPlaceholder = new StringBuilder();
    for(String key : dataList.getFirst().keySet()){
      columns.append(key).append(",");
      valuesPlaceholder.append("?,");
    }
    columns.append("createdAt");
    valuesPlaceholder.append("?");
    String query = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + valuesPlaceholder + ")";

    try (Connection connection = postgresManager.getConnection(databaseCredentials);
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      for (Map<String, String> data : dataList) {
        int index = 1;
        for (String key : data.keySet()) {
          preparedStatement.setString(index++, data.get(key));
        }
        preparedStatement.setTimestamp(index++, new Timestamp(new Date().getTime()));
        preparedStatement.addBatch();
      }
      preparedStatement.executeBatch();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
