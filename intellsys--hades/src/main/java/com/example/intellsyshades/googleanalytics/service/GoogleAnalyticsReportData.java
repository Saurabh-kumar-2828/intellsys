package com.example.intellsyshades.googleanalytics.service;

import com.google.analytics.data.v1beta.*;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GoogleAnalyticsReportData {
  public List<Map<String, String>> getAnalyticsData(List<Metric> metrics, List<Dimension> dimensions, String startDate, String endDate, String propertyId, String accessToken) throws IOException {
    try (BetaAnalyticsDataClient analyticsDataClient = createAnalyticsDataClient(accessToken)) {
      RunReportRequest request = RunReportRequest.newBuilder()
          .setProperty("properties/" + propertyId)
          .addDateRanges(DateRange.newBuilder().setStartDate("2024-03-01").setEndDate("2024-03-31"))
          .addAllDimensions(dimensions)
          .addAllMetrics(metrics)
          .build();

      RunReportResponse response = analyticsDataClient.runReport(request);
      List<Row> rows = response.getRowsList();

      return mapAnalyticsData(rows, response);
    }
  }

  public List<Map<String, String>> mapAnalyticsData(List<Row> rows, RunReportResponse response){
    List<Map<String, String>> mappedData = new ArrayList<>();
    Map<String, String> dimensionNamesMap = new HashMap<>();
    // Map dimension names to their IDs
    for (int i = 0; i < response.getDimensionHeadersList().size(); i++) {
      DimensionHeader dimensionHeader = response.getDimensionHeaders(i);
      dimensionNamesMap.put("dimension_" + i, dimensionHeader.getName());
    }
    // Map metric names to their IDs
    for (int i = 0; i < response.getMetricHeadersList().size(); i++) {
      MetricHeader metricHeader = response.getMetricHeaders(i);
      dimensionNamesMap.put("metric_" + i, metricHeader.getName());
    }
    // Map rows to a list of maps, with each map representing a row of data
    for (Row row : rows) {
      Map<String, String> rowData = new HashMap<>();
      // Map dimension values to their respective names
      for (int i = 0; i < row.getDimensionValuesCount(); i++) {
        String dimensionId = "dimension_" + i;
        String dimensionName = dimensionNamesMap.get(dimensionId);
        String dimensionValue = row.getDimensionValues(i).getValue();
        rowData.put(dimensionName, dimensionValue);
      }
      // Map metric values to their respective names
      for (int i = 0; i < row.getMetricValuesCount(); i++) {
        String metricId = "metric_" + i;
        String metricName = dimensionNamesMap.get(metricId);
        String metricValue = row.getMetricValues(i).getValue();
        rowData.put(metricName, metricValue);
      }
      // Add the row data to the list
      mappedData.add(rowData);
    }
    return mappedData;
  }

  private BetaAnalyticsDataClient createAnalyticsDataClient(String accessToken) throws IOException {
    AccessToken token = new AccessToken(accessToken, null);
    GoogleCredentials credentials = GoogleCredentials.create(token);
    BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
    return BetaAnalyticsDataClient.create(settings);
  }

  public List<Map<String, String>> mapAnalyticsDataToColumns(List<Map<String, String>> tableDetails, List<Map<String, String>> reports) {
    List<Map<String, String>> mappedData = new ArrayList<>();

    for(Map<String, String> report: reports){
      // report = {audienceName=All Users, date=20240301, sessions=187, sessionConversionRate=0}
      Map<String, String> mappedRow = new HashMap<>();
      for(Map.Entry<String, String> entry: report.entrySet()){
        // entry = audienceName=All Users
        String columnName = getColumnForName(tableDetails, entry.getKey());
        if (columnName != null) {
          mappedRow.put(columnName, entry.getValue());
        }
      }
      mappedData.add(mappedRow);
    }
    return mappedData;
  }

  private String getColumnForName(List<Map<String, String>> tableDetails, String name) {
    for (Map<String, String> tableDetail : tableDetails) {
      // {columnName=conversions, columnType=INTEGER, parameterType=metrics, parameterName=conversions}
      if (tableDetail.containsKey("parameterName") && tableDetail.get("parameterName").equals(name)) {
        return tableDetail.get("columnName");
      }
    }
    return null;
  }
}