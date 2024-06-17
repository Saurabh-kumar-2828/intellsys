package com.example.intellsyshades.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

@Component
public class PostgresManager {
  private String host;
  private String port;
  private final Cryptr cryptr;

  private enum DatabaseId {
    STORAGE_1("06fff6bb-33b2-4c93-895c-0d94625149bf"),
    STORAGE_2("53e901f8-6f6e-4bae-ab85-9f4851265664");

    private final String id;
    DatabaseId(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    public static DatabaseId fromId(String id) {
      for (DatabaseId dbId : DatabaseId.values()) {
        if (dbId.getId().equals(id)) {
          return dbId;
        }
      }
      throw new IllegalArgumentException("No such database id: " + id);
    }
  }

  public PostgresManager(Cryptr cryptr){
    this.cryptr = cryptr;
  }
  public Connection getConnection(String encryptedCredentials) throws Exception {
    String name;
    String username;
    String password;
    try {
      System.out.println("encryptedCredentials: "+encryptedCredentials);
      String decryptedCredentials = cryptr.decryptViaApi(encryptedCredentials);
//      String decryptedCredentials = "{\"HOST_ID\":\"06fff6bb-33b2-4c93-895c-0d94625149bf\",\"DB_USERNAME\":\"769d3425-30dd-42b1-8d80-8db4a30a574c\",\"DB_PASSWORD\":\"93a4ac2e-d6ff-46e7-b993-6706ddee003b\",\"DB_NAME\":\"769d3425-30dd-42b1-8d80-8db4a30a574c\"}";
//      System.out.println("decryptedCredentials: "+decryptedCredentials);
      Map<String, String> credentialMap = parseDecryptedCredentials(decryptedCredentials);
      System.out.println("credentialMap: "+credentialMap);
      DatabaseId databaseId = DatabaseId.fromId(credentialMap.get("HOST_ID"));
      System.out.println("databaseId: "+databaseId);
      resolveHost(databaseId);
      username = credentialMap.get("DB_USERNAME");
      password = credentialMap.get("DB_PASSWORD");
      name = credentialMap.get("DB_NAME");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
      String dbUrl = "jdbc:postgresql://" + this.host + ":" + this.port + "/" + name;
      return DriverManager.getConnection(dbUrl, username, password);
   }
  private void resolveHost(DatabaseId hostId) throws IOException {
    switch(hostId){
      case STORAGE_1 :
        this.host = System.getenv("STORAGE_HOST_1");
        this.port = System.getenv("STORAGE_PORT_1");
        break;
        case STORAGE_2 :
          this.host = System.getenv("STORAGE_HOST_2");
          this.port = System.getenv("STORAGE_PORT_2");
          break;
      default:
        throw new IllegalArgumentException("No such database id: " + hostId);
    }
  }

  private Map<String, String> parseDecryptedCredentials(String decryptedCredentials) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(decryptedCredentials, new TypeReference<Map<String, String>>() {});
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.emptyMap();
    }
  }
}
