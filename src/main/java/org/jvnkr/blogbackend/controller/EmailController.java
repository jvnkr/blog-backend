package org.jvnkr.blogbackend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.JwtAuthResponseDto;
import org.jvnkr.blogbackend.dto.VerifyRegisterDto;
import org.jvnkr.blogbackend.service.AuthService;
import org.jvnkr.blogbackend.utils.Encode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/email")
public class EmailController {
  private AuthService authService;

  @GetMapping("/verify")
  public void verifyEmail(@RequestParam("t") String verifyToken, HttpServletResponse response) throws IOException {
    String decodedToken = Encode.decodeToken(verifyToken);
    JwtAuthResponseDto res = authService.verifyRegister(new VerifyRegisterDto(decodedToken));

    // Set the tokens as HTTP cookies
    Cookie accessTokenCookie = new Cookie("a_t", res.getAccessToken());
//    accessTokenCookie.setHttpOnly(true);
//    accessTokenCookie.setSecure(true); // Use this flag if your site uses HTTPS
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(60); // Set cookie expiration time in seconds

    Cookie refreshTokenCookie = new Cookie("r_t", res.getRefreshToken());
//    refreshTokenCookie.setHttpOnly(true);
//    refreshTokenCookie.setSecure(true); // Use this flag if your site uses HTTPS
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24); // Set cookie expiration time in seconds

    response.addCookie(accessTokenCookie);
    response.addCookie(refreshTokenCookie);

    // TODO: Make verification success page which gets tokens and logs the user in
    // String redirectUrl = "http://localhost:3000/verification-success";

    String redirectUrl = "http://localhost:3000";

    // Redirects on the frontend
    response.sendRedirect(redirectUrl);
  }
}
