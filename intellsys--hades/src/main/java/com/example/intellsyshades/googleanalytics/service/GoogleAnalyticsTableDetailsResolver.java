package com.example.intellsyshades.googleanalytics.service;

import com.example.intellsyshades.common.TableDetailsResolver;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class GoogleAnalyticsTableDetailsResolver implements TableDetailsResolver {

  @Override
  public Map<String, List<Map<String, String>>> getTableDetails(int tier){
    ClassLoader classLoader = getClass().getClassLoader();
    ObjectMapper mapper = new ObjectMapper();
    Map<String, List<Map<String, String>>> resolvedTableDetails = new LinkedHashMap<>();

    try{
      InputStream dimensionsInputStream = classLoader.getResourceAsStream("googleAnalytics/dimensions.json");
      Map<String, Map<String, String>> dimensionData = mapper.readValue(dimensionsInputStream, new TypeReference<>() {
      });
      System.out.println("Dimensions File Loaded!");
      InputStream metricsInputStream = classLoader.getResourceAsStream("googleAnalytics/metrics.json");
      Map<String, Map<String,String>> metricData = mapper.readValue(metricsInputStream, new TypeReference<>() {
      });
      System.out.println("Metrics File Loaded!");
      InputStream tableDetailsInputStream = classLoader.getResourceAsStream("googleAnalytics/tableDetails.json");
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
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return resolvedTableDetails;
  }
}
