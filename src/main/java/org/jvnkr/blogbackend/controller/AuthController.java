package org.jvnkr.blogbackend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.*;
import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

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

    // Login and get the tokens using authService
    JwtAuthResponseDto tokens = authService.login(userDto, response);

    // Return the response entity as usual
    return ResponseEntity.status(HttpStatus.OK).body(tokens);
  }

  @PostMapping("/session")
  public ResponseEntity<SessionTokenDto> validate(@CookieValue(value = "a_t", defaultValue = " ") String accessToken,
                                                  @CookieValue(value = "r_t", defaultValue = " ") String refreshToken, HttpServletResponse response, HttpServletRequest request) {
    // Get cookies from request
    Cookie[] cookies = request.getCookies();
    System.out.println(Arrays.toString(cookies));
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        System.out.println("Cookie Name: " + cookie.getName());
        System.out.println("Cookie Value: " + cookie.getValue());
      }
    }
    if (refreshToken.isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Refresh Token is required");
    }

    ValidateTokensDto sessionTokenDto = new ValidateTokensDto(accessToken, refreshToken);
    SessionTokenDto newToken = authService.validateSession(sessionTokenDto, response);
    return ResponseEntity.status(HttpStatus.OK).body(newToken);
  }
}
