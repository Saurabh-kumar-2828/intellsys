//package com.example.intellsyshades.googleads.service;
//
//import com.google.ads.googleads.lib.GoogleAdsClient;
//import com.google.ads.googleads.v16.services.GoogleAdsRow;
//import com.google.ads.googleads.v16.services.GoogleAdsServiceClient;
//import com.google.ads.googleads.v16.services.SearchGoogleAdsRequest;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class GoogleAdsData {
//
//  private final GoogleAdsClient googleAdsClient;
//
//  public GoogleAdsData(GoogleAdsClient googleAdsClient) {
//    this.googleAdsClient = googleAdsClient;
//  }
//
//  public List<GoogleAdsData> fetchData(
//      String sqlQuery,
//      String accountId,
//      String customerAccountId,
//      String accessToken) throws Exception {
//    try(GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()){
//      SearchGoogleAdsRequest request = SearchGoogleAdsRequest.newBuilder()
//          .setCustomerId(customerAccountId)
//          .setQuery(sqlQuery)
//          .build();
//
//      Iterable<GoogleAdsRow> response = googleAdsServiceClient.search(request).iterateAll();
//      List<GoogleAdsData> googleAdsDataList = new ArrayList<>();
//      for (GoogleAdsRow googleAdsRow : response) {
//        GoogleAdsData googleAdsData = new GoogleAdsData();
//        googleAdsData.setData(googleAdsRow.toMap());
//        googleAdsDataList.add(googleAdsData);
//      }
//
//      return googleAdsDataList;
//    }
//  }
//}
