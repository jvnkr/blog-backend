package org.jvnkr.blogbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jvnkr.blogbackend.exception.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
//    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    // Log the exception for internal purposes
    logger.error("Invalid credentials - {}", authException.getMessage(), authException);

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ErrorResponse errorResponse = new ErrorResponse("Invalid credentials");
//    ErrorResponse errorResponse = new ErrorResponse(authException.getMessage());
    response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
  }
}
