package org.jvnkr.blogbackend.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponseDto {
  private String accessToken;
  private String refreshToken;
  private String username;
  private String name;
  private UUID userId;
  // private String tokenType = "Bearer";
}
