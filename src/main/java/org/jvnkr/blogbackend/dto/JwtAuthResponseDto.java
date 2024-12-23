package org.jvnkr.blogbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponseDto {
  private String accessToken;
  private String refreshToken;
  private String username;
  private String name;
  private String email;
  private UUID userId;
  private boolean verified;
  // private String tokenType = "Bearer";
}
