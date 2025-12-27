package io.github.elpis.reactive.websockets.processor.util;

import io.github.elpis.reactive.websockets.processor.exception.WebSocketProcessorException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {
  public static final HashUtils INSTANCE = new HashUtils();

  private HashUtils() {}

  public String generateHash(String... keys) {
    final String uniqueKey = String.join(".", keys);

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(uniqueKey.getBytes());
      return this.toHexString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new WebSocketProcessorException(e.getMessage());
    }
  }

  private String toHexString(byte[] hash) {
    BigInteger number = new BigInteger(1, hash);

    StringBuilder hexString = new StringBuilder(number.toString(16));

    while (hexString.length() < 64) {
      hexString.insert(0, '0');
    }

    return hexString.toString();
  }
}
