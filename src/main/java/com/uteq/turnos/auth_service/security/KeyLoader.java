package com.uteq.turnos.auth_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;

@Component
public class KeyLoader {
  @Value("${security.jwt.key.private-path}") private String privatePath;
  @Value("${security.jwt.key.public-path}")  private String publicPath;

  public PrivateKey loadPrivateKey() {
    try (InputStream is = new ClassPathResource(privatePath).getInputStream()) {
      byte[] keyBytes = is.readAllBytes();
      String pem = new String(keyBytes, StandardCharsets.UTF_8)
          .replaceAll("-----BEGIN (.*)-----", "")
          .replaceAll("-----END (.*)-----", "")
          .replaceAll("\\s", "");
      byte[] decoded = java.util.Base64.getDecoder().decode(pem);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return kf.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    } catch (Exception e) { throw new RuntimeException("Error loading private key", e); }
  }

  public PublicKey loadPublicKey() {
    try (InputStream is = new ClassPathResource(publicPath).getInputStream()) {
      byte[] keyBytes = is.readAllBytes();
      String pem = new String(keyBytes, StandardCharsets.UTF_8)
          .replaceAll("-----BEGIN (.*)-----", "")
          .replaceAll("-----END (.*)-----", "")
          .replaceAll("\\s", "");
      byte[] decoded = java.util.Base64.getDecoder().decode(pem);
      KeyFactory kf = KeyFactory.getInstance("RSA");
      return kf.generatePublic(new X509EncodedKeySpec(decoded));
    } catch (Exception e) { throw new RuntimeException("Error loading public key", e); }
  }
}
