package com.example.intellsyshades.googleads.service;

import com.example.intellsyshades.common.TableDetailsResolver;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class GoogleAdsTableDetailsResolver implements TableDetailsResolver {

  @Override
  public Map<String, List<Map<String,String>>> getTableDetails(int tier){
    ClassLoader classLoader = getClass().getClassLoader();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, List<Map<String,String>>> resolvedTableDetails = new HashMap<>();
    try{
      InputStream dimensionsInputStream = classLoader.getResourceAsStream("googleAds/dimensions.json");
      Map<String, Map<String,String>> dimensionData = mapper.readValue(dimensionsInputStream, new TypeReference<>() {
      });
      System.out.println("Dimension File Loaded!");
      InputStream metricsInputStream = classLoader.getResourceAsStream("googleAds/metrics.json");
      Map<String, Map<String,String>> metricData = mapper.readValue(metricsInputStream, new TypeReference<>() {});
      System.out.println("Metric File Loaded!");
      InputStream tableDetailsInputStream = classLoader.getResourceAsStream("googleAds/tableDetails.json");
      Map<String, Map<String, Map<String, List<String>>>> tableDetailsJson = mapper.readValue(tableDetailsInputStream, new TypeReference<>() {
      });
//      {0:{user_acquisition: {metrics: [a7368432,a37bc12d]}}
      System.out.println("Table Details File Loaded!");
      for(Map.Entry<String, Map<String, Map<String, List<String>>>> entry: tableDetailsJson.entrySet()){
        int currentTier = Integer.parseInt(entry.getKey());
        if(currentTier > tier){
          break;
        }
        Map<String, Map<String, List<String>>> tables = entry.getValue();
        for (Map.Entry<String, Map<String, List<String>>> tableEntry : tables.entrySet()) {
          String tableName = tableEntry.getKey();
          Map<String, List<String>> details = tableEntry.getValue();
          List<Map<String,String>> columnDetails = new ArrayList<>();
          for (Map.Entry<String, List<String>> detailEntry : details.entrySet()) {
            String detailType = detailEntry.getKey();
            List<String> ids = detailEntry.getValue();
            for (String id : ids) {
              Map<String, String> columnDetail = new LinkedHashMap<>();
              Map<String, String> sourceData = detailType.equals("dimensions") ? dimensionData.get(id) : metricData.get(id);
              if (sourceData != null) {
                columnDetail.put("columnName", sourceData.get("columnName"));
                columnDetail.put("columnType", sourceData.get("columnType"));
                columnDetail.put("parameterType", detailType);
                columnDetail.put("parameterName", sourceData.get("name"));
                columnDetails.add(columnDetail);
              }
            }
          }
          resolvedTableDetails.put(tableName, columnDetails);
        }
      }
    } catch (IOException e){
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return resolvedTableDetails;
  }

  /**
   *
   * @param tableValues
   * @return List of Metrics
   */
  public List<String> getMetrics(List<Map<String, String>> tableValues){
    List<String> metrics = new ArrayList<>();
    for(Map<String, String> tableValue : tableValues){
      String parameterName = tableValue.get("parameterName");
      String parameterType = tableValue.get("parameterType");
      if(parameterType.equals("metrics")){
        metrics.add(parameterName);
      }
    }
    return metrics;
  }

  /**
   *
   * @param tableValues
   * @return List of Dimensions
   */
  public List<String> getDimensions(List<Map<String, String>> tableValues){
    List<String> dimensions = new ArrayList<>();
    for(Map<String, String> tableValue : tableValues){
      String parameterName = tableValue.get("parameterName");
      String parameterType = tableValue.get("parameterType");
      if(parameterType.equals("dimensions")){
        dimensions.add(parameterName);
      }
    }
    return dimensions;
  }

  /**
   *
   * @param startDate startDate in YYYY-MM-DD format
   * @param endDate endDate in YYYY-MM-DD format
   * @param tableName Name of the SQL Table in intellsys-storage
   * @return SQL query for google ads api sdk.
   */
  public String queryBuilder(List<Map<String, String>> tableValues, String startDate, String endDate, String tableName){
    String googleAdsTableName = getGoogleAdsTableName(tableName);
    // Get Metrics
    List<String> metrics = getMetrics(tableValues);
    // Get Dimensions
    List<String> dimensions = getDimensions(tableValues);
    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT ");
    queryBuilder.append(String.join(", ", dimensions));
    queryBuilder.append(", ");
    queryBuilder.append(String.join(", ", metrics));
    queryBuilder.append(" FROM").append(googleAdsTableName).append(" ");
    queryBuilder.append("WHERE segments.date BETWEEN '").append(startDate).append("' AND '").append(endDate).append("'");
    return queryBuilder.toString();
  }

  private String getGoogleAdsTableName(String tableName) {
    ClassLoader classLoader = getClass().getClassLoader();
    ObjectMapper mapper = new ObjectMapper();
    try {
      InputStream googleAdsTableInputStream = classLoader.getResourceAsStream("googleAds/" + "googleAdsTables.json");
      Map<String, String> googleAdsTablesData = mapper.readValue(googleAdsTableInputStream, new TypeReference<Map<String, String>>() {
      });
      System.out.println("Google Ads Table to Intellsys Tables Mapped JSON loaded!");
      if(googleAdsTablesData == null){
        throw new NullPointerException("Google Ads Table Mapper JSON is empty!");
      }
      for(Map.Entry<String,String> tableMapper: googleAdsTablesData.entrySet()){
        if(Objects.equals(tableMapper.getKey(), tableName)){
          return tableMapper.getValue();
        }
      }
      throw new IOException("Cannot find Google Ads Table name with table name: "+ tableName);
    } catch (IOException e){
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
