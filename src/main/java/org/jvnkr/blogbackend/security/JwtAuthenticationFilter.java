package org.jvnkr.blogbackend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jvnkr.blogbackend.exception.APIException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;

  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
          throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String accessToken = getAccessToken(request);
    String refreshToken = getRefreshToken(request);

    try {
      jwtTokenProvider.validateToken(refreshToken);

      if (accessToken != null) {
        if (jwtTokenProvider.validateToken(accessToken) != null) {
          UUID accessTokenUserId = jwtTokenProvider.getUserId(accessToken);
          logger.debug("Valid access token found");

          if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken) != null) {
            UUID refreshTokenUserId = jwtTokenProvider.getUserId(refreshToken);
            if (!accessTokenUserId.equals(refreshTokenUserId)) {
              throw new APIException(HttpStatus.UNAUTHORIZED, "Token user mismatch");
            }
          }

          setAuthenticationContext(accessToken, request);
        }
      }
    } catch (ExpiredJwtException e) {
      logger.error("Expired JWT Token: {}", e);
      throw new APIException(HttpStatus.UNAUTHORIZED, "Expired refresh token");
    } catch (SignatureException e) {
      throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid access token");
    } catch (JwtException e) {
      logger.error("Error processing JWT token: ", e);
      throw new APIException(HttpStatus.UNAUTHORIZED, "Invalid access token");
    }

    filterChain.doFilter(request, response);
  }

  private void setAuthenticationContext(String token, HttpServletRequest request) {
    UUID userId = jwtTokenProvider.getUserId(token);
    UserDetails userDetails = customUserDetailsService.loadUserById(userId);

    if (userDetails == null) {
      logger.error("User details could not be loaded for userId: " + userId);
      return;
    }

    List<SimpleGrantedAuthority> authorities = jwtTokenProvider.getClaimFromToken(token, claims -> {
      @SuppressWarnings("unchecked")
      List<String> roles = (List<String>) claims.get("roles");
      return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    });

    if (authorities.isEmpty()) {
      logger.warn("No authorities found for user: " + userDetails.getUsername());
    }

    UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    logger.debug("Authentication context set with user details: " + userDetails.getUsername());
  }

  private String getAccessToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  private String getRefreshToken(HttpServletRequest request) {
    return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
            .filter(cookie -> "r_t".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
  }
}