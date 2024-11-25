package org.jvnkr.blogbackend.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Encode {
  public static String encodeToken(String token) {
    return Base64.getUrlEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
  }

  public static String decodeToken(String encodedToken) {
    byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedToken);
    return new String(decodedBytes, StandardCharsets.UTF_8);
  }

}
