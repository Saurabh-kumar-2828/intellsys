package com.example.intellsyshades.datasource.controller;

import com.example.intellsyshades.common.dto.DataSourceDTO;
import com.example.intellsyshades.datasource.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/data-sources")
public class DataSourceController {

  private final DataSourceService dataSourceService;

  @Autowired
  public DataSourceController(DataSourceService dataSourceService) {
    this.dataSourceService = dataSourceService;
  }

  @PostMapping("/create")
  public ResponseEntity<Object> createDataSource(@RequestBody DataSourceDTO dataSourceDTO){
    try{
      dataSourceService.createDataSource(dataSourceDTO);
      return ResponseEntity.ok(dataSourceDTO);
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating data source: "+ e.getMessage());
    }
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<String> deleteDataSource(@PathVariable UUID id){
    try{
      dataSourceService.deleteDataSource(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }catch(Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting data source: "+ e.getMessage());
    }
  }
}
