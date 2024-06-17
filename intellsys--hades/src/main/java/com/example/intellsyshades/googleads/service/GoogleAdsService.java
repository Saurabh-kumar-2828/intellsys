package com.example.intellsyshades.googleads.service;

import com.example.intellsyshades.common.GoogleCommon;
import com.example.intellsyshades.common.dto.DataSourceCredentials;
import com.example.intellsyshades.datasource.repository.CompanyRepository;
import com.example.intellsyshades.datasource.repository.DataSourceRepository;
import com.example.intellsyshades.googleads.dto.GoogleAdsCredentials;
import com.example.intellsyshades.service.Cryptr;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GoogleAdsService {

  private final GoogleCommon googleCommon;
  private final DataSourceRepository dataSourceRepository;
  private final GoogleAdsTableDetailsResolver googleAdsTableDetailsResolver;
  private final Cryptr cryptr;
  private final CompanyRepository companyRepository;
//  private final GoogleAdsData googleAdsData;

  public GoogleAdsService(GoogleCommon googleCommon, DataSourceRepository dataSourceRepository, GoogleAdsTableDetailsResolver googleAdsTableDetailsResolver, Cryptr cryptr, CompanyRepository companyRepository) {
    this.googleCommon = googleCommon;
    this.dataSourceRepository = dataSourceRepository;
    this.googleAdsTableDetailsResolver = googleAdsTableDetailsResolver;
    this.cryptr = cryptr;
    this.companyRepository = companyRepository;
//    this.googleAdsData = googleAdsData;
  }

  public String generateOAuthUrl(String callbackUrl, String state){
    String scopes = "https://www.googleapis.com/auth/adwords https://www.googleapis.com/auth/userinfo.email";
    return googleCommon.getOAuthUrl(callbackUrl, state, scopes);
  }

  public String getRefreshTokenFromAuthCode(String authorizationCode,String callbackUrl){
    try {
      return googleCommon.getRefreshToken(authorizationCode, callbackUrl);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public void initialLoad(UUID dataSourceId, String startDate, String endDate){
    try{
      DataSourceCredentials dataSourceCredentials = dataSourceRepository.getDataSourceCredentialsDetailsById(dataSourceId);
      List<UUID> cursors = dataSourceRepository.getAllCursorsForDataSource(dataSourceId);
      for(UUID cursorId : cursors){
        cursorUpdate(cursorId, dataSourceCredentials, startDate, endDate);
      }
    } catch (Exception e){
      throw new RuntimeException(e);
    }
  }

  public void cursorUpdate(UUID cursorId, DataSourceCredentials dataSourceCredentials, String startDateStr, String endDateStr){
    try{
      // Spawn new thread
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
      Date startDate = dateFormat.parse(startDateStr);
      Date endDate = dateFormat.parse(endDateStr);
      GoogleAdsCredentials googleAdsCredentials = mapToGoogleAdsCredentials(dataSourceCredentials.getSourceCredentials());
      AccessToken accessToken = googleCommon.getAccessToken(googleAdsCredentials.getRefreshToken());
      googleAdsCredentials.setAccessToken(accessToken.getTokenValue());
      Integer subscriptionTier = dataSourceRepository.getSubscriptionTierFromCursorId(cursorId);
      // Get dimensions and metrics depending upon tier.
      Map<String, List<Map<String, String>>> tableDetails = googleAdsTableDetailsResolver.getTableDetails(subscriptionTier);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(endDate);
      while (!calendar.getTime().before(startDate)){
        Date currentEndDate = calendar.getTime();
        calendar.add(Calendar.DATE, -7);
        Date currentStartDate = calendar.getTime();
        // Fetch data for each table. (Get Metrics and Dimensions)
        for(Map.Entry<String, List<Map<String, String>>> tableDetail : tableDetails.entrySet()){
          String tableName = tableDetail.getKey();
          List<Map<String, String>> tableValues = tableDetail.getValue();
          String sqlQuery = googleAdsTableDetailsResolver.queryBuilder(tableValues, dateFormat.format(startDate), dateFormat.format(endDate), tableName);

          // Get Data from SDK
//          List<GoogleAdsData> adsData = googleAdsData.fetchData(sqlQuery, googleAdsCredentials.getAccountId(), googleAdsCredentials.getCustomerAccountId(), googleAdsCredentials.getAccessToken());
          // Ingest Data
        }
        // Update cursor
        dataSourceRepository.updateCursor(cursorId,currentStartDate.toString());
      }
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  private GoogleAdsCredentials mapToGoogleAdsCredentials(String sourceCredentials) throws Exception {
    String decryptedCredentials = cryptr.decryptViaApi(sourceCredentials);
    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, String> credentialMap = objectMapper.readValue(decryptedCredentials, new TypeReference<Map<String, String>>() {
    });
    String refreshToken = credentialMap.get("refreshToken");
    String googleAccountId = credentialMap.get("googleAccountId");
    String googleLoginCustomerId = credentialMap.get("googleLoginCustomerId");

    GoogleAdsCredentials googleAdsCredentials = new GoogleAdsCredentials();
    googleAdsCredentials.setRefreshToken(refreshToken);
    googleAdsCredentials.setAccountId(googleAccountId);
    googleAdsCredentials.setCustomerAccountId(googleLoginCustomerId);
    return googleAdsCredentials;
  }
}
