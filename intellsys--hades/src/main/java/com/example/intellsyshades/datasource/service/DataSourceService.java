package com.example.intellsyshades.datasource.service;

import com.example.intellsyshades.IntegrationType;
import com.example.intellsyshades.common.dto.DataSourceDTO;
import com.example.intellsyshades.datasource.repository.CompanyRepository;
import com.example.intellsyshades.datasource.repository.DataSourceRepository;
import com.example.intellsyshades.googleanalytics.service.GoogleAnalyticsTableDetailsResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DataSourceService {

  private final DataSourceRepository dataSourceRepository;
  private final CompanyRepository companyRepository;
  private final GoogleAnalyticsTableDetailsResolver googleAnalyticsTableDetailsResolver;


  @Autowired
  public DataSourceService(DataSourceRepository dataSourceRepository, CompanyRepository companyRepository, GoogleAnalyticsTableDetailsResolver googleAnalyticsTableDetailsResolver) {
    this.dataSourceRepository = dataSourceRepository;
    this.companyRepository = companyRepository;
    this.googleAnalyticsTableDetailsResolver = googleAnalyticsTableDetailsResolver;
  }

  @Transactional
  public void createDataSource(DataSourceDTO dataSourceDTO) {
    try{
      dataSourceRepository.insertDataSource(dataSourceDTO);
      Integer subscriptionTier = companyRepository.getSubscriptionTier(dataSourceDTO.getCompanyId());
      dataSourceRepository.insertSubDataSource(dataSourceDTO, subscriptionTier);
      dataSourceRepository.assignDataSourcePermissionsToCreator(dataSourceDTO.getUserId(),dataSourceDTO.getId());
      dataSourceRepository.mapCompanyToDataSource(dataSourceDTO.getCompanyId(),dataSourceDTO.getId());
      System.out.println("Created new DataSource");
      System.out.println("Trying to create New Tables.");
      createTable(dataSourceDTO);
    }
    catch(Exception e){
      throw new RuntimeException(e.getMessage());
    }
  }

  private void createTable(DataSourceDTO dataSourceDTO){
    try{
      Integer subscriptionTier = companyRepository.getSubscriptionTier(dataSourceDTO.getCompanyId());
      String databaseCredentials = companyRepository.getCompanyDbCredentials(dataSourceDTO.getCompanyId());
//      UUID integrationType = dataSourceDTO.getIntegrationType();
//      TODO: DO this for every integrationType
//      Map<String, List<Map<String, String>>> tableDetails = IntegrationType.getTableDetails(integrationType).getTableDetails(subscriptionTier);
      Map<String, List<Map<String, String>>> tableDetails = googleAnalyticsTableDetailsResolver.getTableDetails(subscriptionTier);
      System.out.println("tableDetails: " + tableDetails);
      Map<UUID, String> tableMap = companyRepository.createTables(tableDetails, databaseCredentials);
//      System.out.println("Table Ids: " + tableMap);
      dataSourceRepository.insertStorageTableIds(dataSourceDTO, tableMap);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void dropTable(UUID dataSourceId){

  }

  public void deleteDataSource(UUID id) {
    try {
      dataSourceRepository.deleteDataSourceById(id);
      }
    catch(Exception e){
      throw new RuntimeException(e.getMessage());
      }
  }
}
