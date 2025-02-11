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
public class SessionTokenDto {
  private String accessToken;
  private UUID userId;
  private String username;
  private String name;
  private String role;
  private String email;
  private String bio;
  private boolean verified;
}
