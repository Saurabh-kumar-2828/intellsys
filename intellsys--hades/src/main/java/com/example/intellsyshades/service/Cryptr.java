package com.example.intellsyshades.service;

import jakarta.xml.bind.DatatypeConverter;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

@Component
public class Cryptr {
  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int IV_LENGTH = 16;
  private static final int TAG_LENGTH = 16;
  private static final int SALT_LENGTH = 64;
  private static final int PBKDF2_ITERATIONS = 100000;
  private static final String ENCODING = "UTF-8";
  private static final int KEY_LENGTH = 256;
  private final String secret;

  public Cryptr(Environment environment) {
    this.secret = environment.getProperty("ENCRYPTION_KEY");
  }

  public String encrypt(String value) throws Exception {
    if (value == null) {
      throw new IllegalArgumentException("Value must not be null");
    }

    byte[] iv = generateRandomBytes(IV_LENGTH);
    byte[] salt = generateRandomBytes(SALT_LENGTH);
    byte[] key = generateKey(salt);

    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_LENGTH * 8, iv));
    byte[] encrypted = cipher.doFinal(value.getBytes(ENCODING));

    return Base64.getEncoder().encodeToString(concatArrays(concatArrays(salt, iv), encrypted));
  }

  public String decryptViaApi(String value) throws Exception {
    String apiUrl = "https://www.intellsys.ai/decrypt";
    String requestBody = "{\"value\": \"" + value + "\"}";
    try{
    HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(apiUrl))
          .header("Content-Type", "application/json")
          .header("Authorization", "Basic !dk9qX!MQEz75q4r") // Your authorization header
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println("response: "+response);
      if(response.statusCode() == 200){
        String responseBody = response.body();
        JSONObject jsonResponse = new JSONObject(responseBody);

        return jsonResponse.getString("decryptedValue");

      } else {
        throw new RuntimeException("Failed to decrypt value. HTTP Status Code: " + response.statusCode());
      }
    } catch(Exception e){
      throw new RuntimeException("Failed to decrypt value: " + e.getMessage(), e);
    }
  }
  public String decrypt(String value) throws Exception {
    // TODO: Fix decrypt function.
    if (value == null) {
      throw new IllegalArgumentException("Value must not be null");
    }
    System.out.println("Value: " + value);
    int tagPosition = SALT_LENGTH + IV_LENGTH;
    int encryptedPosition = tagPosition + TAG_LENGTH;
    byte[] decodedValue = DatatypeConverter.parseHexBinary(value);
    byte[] salt = copyOfRange(decodedValue, 0, SALT_LENGTH);
    byte[] iv = copyOfRange(decodedValue, SALT_LENGTH, tagPosition);
    byte[] tag = copyOfRange(decodedValue, tagPosition, encryptedPosition);
    byte[] encrypted = copyOfRange(decodedValue, encryptedPosition, decodedValue.length);

    byte[] key = generateKey(salt);

    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_LENGTH * 8, iv));
    cipher.updateAAD(tag);
    byte[] decrypted = cipher.doFinal(encrypted);
    System.out.println("decrypted: "+ Arrays.toString(decrypted));

    return new String(decrypted, ENCODING);
  }

  private byte[] generateKey(byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
    return factory.generateSecret(spec).getEncoded();
  }

  private byte[] generateRandomBytes(int length) {
    byte[] bytes = new byte[length];
    new SecureRandom().nextBytes(bytes);
    return bytes;
  }

  private byte[] concatArrays(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  private byte[] copyOfRange(byte[] src, int from, int to) {
    byte[] range = new byte[to - from];
    System.arraycopy(src, from, range, 0, range.length);
    return range;
  }

}
