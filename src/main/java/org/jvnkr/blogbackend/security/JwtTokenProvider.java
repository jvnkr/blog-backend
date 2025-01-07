package org.jvnkr.blogbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.jvnkr.blogbackend.dto.RegisterDto;
import org.jvnkr.blogbackend.entity.User;
import org.jvnkr.blogbackend.service.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
  private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
  private final CustomUserDetailsService customUserDetailsService;

  @Value("${app.jwt-secret}")
  private String jwtSecret;

  @Value("${app.jwt-access-expiration-milliseconds}")
  private long jwtAccessExpiration;

  @Value("${app.jwt-refresh-expiration-milliseconds}")
  private long jwtRefreshExpiration;

  @Value("${app.jwt-verify-expiration-milliseconds}")
  private long jwtVerifyExpiration;

  public JwtTokenProvider(CustomUserDetailsService customUserDetailsService) {
    this.customUserDetailsService = customUserDetailsService;
  }

  public String generateVerifyToken(RegisterDto registerDto) {
    Date currentDate = new Date();
    Date expireDate = new Date(currentDate.getTime() + jwtVerifyExpiration);

    List<String> temporaryRoles = Collections.singletonList(Roles.ROLE_USER.toString());

    return Jwts.builder()
            .subject(registerDto.getUsername())
            .claim("username", registerDto.getUsername())
            .claim("password", registerDto.getPassword())
            .claim("name", registerDto.getName())
            .claim("email", registerDto.getEmail())
            .claim("roles", temporaryRoles)
            .issuedAt(currentDate)
            .expiration(expireDate)
            .signWith(key())
            .compact();
  }

  public String generateAccessToken(User user, Authentication authentication) {
    if (user == null) {
      return null;
    }
    Date currentDate = new Date();
    Date expireDate = new Date(currentDate.getTime() + jwtAccessExpiration);

    return Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .claim("roles", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()))
            .issuedAt(currentDate)
            .expiration(expireDate)
            .signWith(key())
            .compact();
  }

  public String generateRefreshToken(User user) {
    if (user == null) {
      return null;
    }
    Date currentDate = new Date();
    Date expireDate = new Date(currentDate.getTime() + jwtRefreshExpiration);

    return Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .issuedAt(currentDate)
            .expiration(expireDate)
            .signWith(key())
            .compact();
  }

  public Authentication getAuthentication(String token) {
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(getUsername(token));
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  private Key key() {
    return Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(jwtSecret)
    );
  }

  public String getUsername(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .getPayload();

    return claims.getSubject();
  }

  public UUID getUserId(String token) {
    try {
      Claims claims = Jwts.parser()
              .setSigningKey(key())
              .build()
              .parseClaimsJws(token)
              .getPayload();

      Object userIdObj = claims.get("userId");
      if (userIdObj instanceof String) {
        // Convert the String to UUID
        return UUID.fromString((String) userIdObj);
      } else {
        throw new IllegalArgumentException("userId is not of type String");
      }
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
    }

    return null;
  }

  private Claims getAllClaimsFromToken(String token) {
    return (Claims) Jwts.parser().setSigningKey(key()).build().parse(token).getPayload();
  }

  public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = getAllClaimsFromToken(token);
    return claimsResolver.apply(claims);
  }

  // Validate JWT Token
  public Claims validateToken(String token) {
    try {
      // Parse JWT with key
      return Jwts.parser()
              .setSigningKey(key()) // Set the signing key
              .build()
              .parseClaimsJws(token) // Parse token
              .getBody(); // Retrieve the claims

    } catch (ExpiredJwtException e) {
      logger.error("Token has expired: {}", e.getMessage(), e);
      throw e;
    } catch (UnsupportedJwtException e) {
      logger.error("Unsupported JWT token: {}", e.getMessage(), e);
      throw e;
    } catch (MalformedJwtException e) {
      logger.error("Malformed JWT token: {}", e.getMessage(), e);
      throw e;
    } catch (SecurityException e) {
      logger.error("JWT signature validation failed: {}", e.getMessage(), e);
      throw e;
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage(), e);
      throw e;
    }
  }
}