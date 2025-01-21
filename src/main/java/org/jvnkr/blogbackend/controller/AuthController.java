package org.jvnkr.blogbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.*;
import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<JwtAuthResponseDto> register(@RequestBody RegisterDto userDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(userDto));
  }

  @PostMapping("/login")
  public ResponseEntity<JwtAuthResponseDto> login(
      @RequestBody LoginDto userDto,
      HttpServletResponse response) {
    JwtAuthResponseDto tokens = authService.login(userDto, response);

    return ResponseEntity.status(HttpStatus.OK).body(tokens);
  }

  @PostMapping("/session")
  public ResponseEntity<SessionTokenDto> validate(@CookieValue(value = "a_t", defaultValue = " ") String accessToken,
      @CookieValue(value = "r_t", defaultValue = " ") String refreshToken, HttpServletResponse response,
      HttpServletRequest request) {
    if (refreshToken.isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Refresh Token is required");
    }

    ValidateTokensDto sessionTokenDto = new ValidateTokensDto(accessToken, refreshToken);
    SessionTokenDto newToken = authService.validateSession(sessionTokenDto, response);
    return ResponseEntity.status(HttpStatus.OK).body(newToken);
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @PostMapping("/logout")
  public ResponseEntity<Boolean> logout(HttpServletResponse response) {
    return ResponseEntity.status(HttpStatus.OK).body(authService.logout(response));
  }
}
