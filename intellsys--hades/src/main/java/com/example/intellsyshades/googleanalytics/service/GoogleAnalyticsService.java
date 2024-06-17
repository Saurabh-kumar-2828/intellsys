package com.example.intellsyshades.googleanalytics.service;

import com.example.intellsyshades.common.GoogleCommon;
import com.example.intellsyshades.common.dto.DataSourceCredentials;
import com.example.intellsyshades.datasource.repository.CompanyRepository;
import com.example.intellsyshades.datasource.repository.DataSourceRepository;
import com.example.intellsyshades.googleanalytics.dto.AnalyticsProperty;
import com.example.intellsyshades.googleanalytics.dto.GoogleAnalyticsCredentials;
import com.example.intellsyshades.service.Cryptr;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.analytics.data.v1beta.Dimension;
import com.google.analytics.data.v1beta.Metric;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.*;
import com.google.api.services.analyticsadmin.v1beta.GoogleAnalyticsAdmin;
import com.google.api.services.analyticsadmin.v1beta.model.GoogleAnalyticsAdminV1betaAccount;
import com.google.api.services.analyticsadmin.v1beta.model.GoogleAnalyticsAdminV1betaListAccountsResponse;
import com.google.api.services.analyticsadmin.v1beta.model.GoogleAnalyticsAdminV1betaListPropertiesResponse;
import com.google.api.services.analyticsadmin.v1beta.model.GoogleAnalyticsAdminV1betaProperty;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GoogleAnalyticsService {

  private final GoogleCommon googleCommon;
  private final DataSourceRepository dataSourceRepository;
  private final Cryptr cryptr;
  private final GoogleAnalyticsTableDetailsResolver googleAnalyticsTableDetailsResolver;
  private final GoogleAnalyticsReportData googleAnalyticsReportData;
  private final CompanyRepository companyRepository;

  public GoogleAnalyticsService(final GoogleCommon googleCommon, DataSourceRepository dataSourceRepository, Cryptr cryptr,GoogleAnalyticsTableDetailsResolver googleAnalyticsTableDetailsResolver,GoogleAnalyticsReportData googleAnalyticsReportData, CompanyRepository companyRepository) {
    this.googleCommon = googleCommon;
    this.dataSourceRepository = dataSourceRepository;
    this.cryptr = cryptr;
    this.googleAnalyticsTableDetailsResolver = googleAnalyticsTableDetailsResolver;
    this.googleAnalyticsReportData = googleAnalyticsReportData;
    this.companyRepository = companyRepository;
  }

  public String generateOAuthUrl(String callbackUrl, String state){
    String scopes = "https://www.googleapis.com/auth/analytics.readonly https://www.googleapis.com/auth/userinfo.email";
    return googleCommon.getOAuthUrl(callbackUrl, state, scopes);
  }

  public String getRefreshTokenFromAuthCode(String authorizationCode,String callbackUrl){
    try{
      return googleCommon.getRefreshToken(authorizationCode,callbackUrl);
    }catch (IOException e){
      throw new RuntimeException(e.getMessage());
    }
  }

  public List<AnalyticsProperty> getProperties(String refreshToken) throws IOException, GeneralSecurityException {
    AccessToken accessToken = googleCommon.getAccessToken(refreshToken);
    GoogleCredentials credentials = GoogleCredentials.create(accessToken);
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
    GoogleAnalyticsAdmin googleAnalyticsAdmin = new GoogleAnalyticsAdmin
        .Builder(httpTransport, GsonFactory.getDefaultInstance(), requestInitializer)
        .setApplicationName("intellsys-dev").build();

    List<AnalyticsProperty> properties = new ArrayList<>();

    GoogleAnalyticsAdminV1betaListAccountsResponse accountsResponse = googleAnalyticsAdmin.accounts().list().execute();

    if(accountsResponse.getAccounts() != null){
      for(GoogleAnalyticsAdminV1betaAccount account: accountsResponse.getAccounts()){
        String accountId = account.getName();
        GoogleAnalyticsAdminV1betaListPropertiesResponse propertiesResponse = googleAnalyticsAdmin.properties().list()
            .setFilter("parent:" + accountId)
            .execute();
        if(propertiesResponse.getProperties() != null){
          for(GoogleAnalyticsAdminV1betaProperty property: propertiesResponse.getProperties()){
            String propertyId = property.getName();
            String propertyName = property.getDisplayName();

            AnalyticsProperty analyticsProperty = new AnalyticsProperty();
            analyticsProperty.setAccountName(account.getDisplayName());
            analyticsProperty.setAccountId(accountId);
            analyticsProperty.setPropertyId(propertyId);
            analyticsProperty.setPropertyName(propertyName);

            properties.add(analyticsProperty);
          }
        }
      }
    }
    return properties;
  }

  public void initialLoad(UUID dataSourceId, String startDate, String endDate){
    try {
      DataSourceCredentials dataSourceCredentials = dataSourceRepository.getDataSourceCredentialsDetailsById(dataSourceId);
      List<UUID> cursors = dataSourceRepository.getAllCursorsForDataSource(dataSourceId);
      for(UUID cursorId : cursors){
        cursorUpdate(cursorId,dataSourceCredentials, startDate, endDate);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public void cursorUpdate(UUID cursorUUID, DataSourceCredentials dataSourceCredentials, String startDateStr, String endDateStr){
    try {
      // Spawn new thread.
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Date startDate = dateFormat.parse(startDateStr);
      Date endDate = dateFormat.parse(endDateStr);
      GoogleAnalyticsCredentials googleAnalyticsCredentials = mapToGoogleAnalyticsCredentials(dataSourceCredentials.getSourceCredentials());
      AccessToken accessToken = googleCommon.getAccessToken(googleAnalyticsCredentials.getRefreshToken());
      googleAnalyticsCredentials.setAccessToken(accessToken.getTokenValue());
      Integer subscriptionTier = dataSourceRepository.getSubscriptionTierFromCursorId(cursorUUID);
      // Get dimensions and metrics depending upon tier.
      Map<String, List<Map<String, String>>> tableDetails = googleAnalyticsTableDetailsResolver.getTableDetails(subscriptionTier);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(endDate);
      while(!calendar.getTime().before(startDate)){
        Date currentEndDate = calendar.getTime();
        calendar.add(Calendar.DATE, -7);
        Date currentStartDate = calendar.getTime();
        // Fetch data for each table. (Get Metrics and Dimensions)
        for(Map.Entry<String, List<Map<String, String>>> tableDetail : tableDetails.entrySet()){
          String tableName = tableDetail.getKey();
          List<Map<String, String>> tableValues = tableDetail.getValue();
          List<Metric> metrics= getMetrics(tableValues);
          List<Dimension> dimensions = getDimensions(tableValues);
          // Get Data from SDK
          List<Map<String, String>> reports = googleAnalyticsReportData.getAnalyticsData(metrics,dimensions, dateFormat.format(startDate), dateFormat.format(endDate), googleAnalyticsCredentials.getPropertyId(), googleAnalyticsCredentials.getAccessToken());
          // Map it with columnNames. [{columnName: data}, {columnName: data} ]
          List<Map<String, String>> mappedColumnsData = googleAnalyticsReportData.mapAnalyticsDataToColumns(tableValues, reports);
          // Ingest Data
          companyRepository.insertDataIntoCompanyDB(mappedColumnsData, dataSourceCredentials.getDatabaseCredentials(),tableName);
        }
        // Update cursor
        dataSourceRepository.updateCursor(cursorUUID,currentStartDate.toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  private GoogleAnalyticsCredentials mapToGoogleAnalyticsCredentials(String sourceCredentials) throws Exception {
    String decryptedCredentials = cryptr.decryptViaApi(sourceCredentials);
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> credentialMap = objectMapper.readValue(decryptedCredentials, new TypeReference<Map<String, String>>() {});
    String refreshToken = credentialMap.get("refreshToken");
    String propertyId = credentialMap.get("propertyId");

    GoogleAnalyticsCredentials googleAnalyticsCredentials = new GoogleAnalyticsCredentials();
    googleAnalyticsCredentials.setRefreshToken(refreshToken);
    googleAnalyticsCredentials.setPropertyId(propertyId);
    return googleAnalyticsCredentials;
  }

  /**
   *
   * @param tableValues
   * @return List of Metrics
   */
  private List<Metric> getMetrics(List<Map<String, String>> tableValues){
    List<Metric> metrics = new ArrayList<>();
    for(Map<String, String> tableValue : tableValues){
      String parameterName = tableValue.get("parameterName");
      String parameterType = tableValue.get("parameterType");
      if(parameterType.equals("metrics")){
        Metric metric = Metric.newBuilder().setName(parameterName).build();
        metrics.add(metric);
      }
    }
    return metrics;
  }

  /**
   *
   * @param tableValues
   * @return List of Dimensions
   */
  private List<Dimension> getDimensions(List<Map<String, String>> tableValues){
    List<Dimension> dimensions = new ArrayList<>();
    for(Map<String, String> tableValue : tableValues){
      String parameterName = tableValue.get("parameterName");
      String parameterType = tableValue.get("parameterType");
      if(parameterType.equals("dimensions")){
        Dimension dimension = Dimension.newBuilder().setName(parameterName).build();
        dimensions.add(dimension);
      }
    }
    return dimensions;
  }

}
