package org.jvnkr.blogbackend.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.jvnkr.blogbackend.dto.*;
import org.jvnkr.blogbackend.entity.Role;
import org.jvnkr.blogbackend.entity.User;
import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.repository.RoleRepository;
import org.jvnkr.blogbackend.repository.UserRepository;
import org.jvnkr.blogbackend.security.JwtTokenProvider;
import org.jvnkr.blogbackend.service.AuthService;
import org.jvnkr.blogbackend.service.EmailService;
import org.jvnkr.blogbackend.service.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final EmailService emailService;

  @Value("${app.environment}")
  private String environment;

  @Value("${app.jwt-access-expiration-seconds}")
  private int accessTokenExpirationSeconds;

  @Value("${app.jwt-refresh-expiration-seconds}")
  private int refreshTokenExpirationSeconds;

  @Autowired
  public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                         AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, EmailServiceImpl emailService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtTokenProvider = jwtTokenProvider;
    this.emailService = emailService;
  }

  private String generateNewAccessToken(User user, Authentication authentication, HttpServletResponse response) {
    String newAccessToken = jwtTokenProvider.generateAccessToken(user, authentication);
    response.addCookie(createAccessCookie(newAccessToken));
    return newAccessToken;
  }

  private SessionTokenDto renewAccessToken(String refreshToken, User user, HttpServletResponse response) {
    Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
    String newAccessToken = generateNewAccessToken(user, authentication, response);

    return new SessionTokenDto(newAccessToken, user.getId(), user.getUsername(), user.getName(), Roles.getHighestRole(user.getRoles()), user.getEmail(), user.getBio(), user.isVerified());
  }

  @Override
  public SessionTokenDto validateSession(ValidateTokensDto validateTokensDto, HttpServletResponse response) {
    if (validateTokensDto == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: session token is required");
    }

    String refreshToken = validateTokensDto.getRefreshToken();
    String accessToken = validateTokensDto.getAccessToken();

    if (refreshToken == null || refreshToken.trim().isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: refresh token is required");
    }

    if (jwtTokenProvider.validateToken(refreshToken) == null || refreshToken.trim().isEmpty()) {
      throw new APIException(HttpStatus.FORBIDDEN, "Refresh Token expired or invalid");
    }

    User user = userRepository.findById(jwtTokenProvider.getUserId(validateTokensDto.getRefreshToken()))
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Invalid user"));

    try {
      if (accessToken.trim().isEmpty()) {
        return renewAccessToken(refreshToken, user, response);
      }
      jwtTokenProvider.validateToken(accessToken);
    } catch (ExpiredJwtException e) {
      return renewAccessToken(refreshToken, user, response);
    } catch (JwtException e) {
      throw new APIException(HttpStatus.FORBIDDEN, "Failed to renew access token");
    }
    return new SessionTokenDto(accessToken, user.getId(), user.getUsername(), user.getName(), Roles.getHighestRole(user.getRoles()), user.getEmail(), user.getBio(), user.isVerified());
  }

  @Override
  public JwtAuthResponseDto register(RegisterDto registerDto) {
    if (registerDto.getUsername() == null || registerDto.getUsername().isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: username is required");
    }

    if (registerDto.getEmail() == null || registerDto.getEmail().isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: email is required");
    }

    if (registerDto.getPassword() == null || registerDto.getPassword().isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: password is required");
    }

    if (registerDto.getName() == null || registerDto.getName().isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: name is required");
    }

    registerDto.setUsername(registerDto.getUsername().trim());
    registerDto.setEmail(registerDto.getEmail().trim());
    registerDto.setName(registerDto.getName().trim());

    if (userRepository.existsByUsername(registerDto.getUsername())) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Username already exists!");
    }

    if (userRepository.existsByEmail(registerDto.getEmail())) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Email already exists!");
    }

    // Prod only
    if (environment.equals("prod")) {
      emailService.sendVerificationEmail(registerDto.getEmail().trim(), registerDto);
    } else {
      createUser(registerDto);
    }

    return null;
  }

  private Cookie createAccessCookie(String accessToken) {
    Cookie accessTokenCookie = new Cookie("a_t", accessToken);
    accessTokenCookie.setHttpOnly(true); // Ensures the cookie is not accessible via JavaScript
    accessTokenCookie.setSecure(false); // Ensures the cookie is sent over HTTPS
    accessTokenCookie.setPath("/"); // Cookie is accessible for the entire application
    accessTokenCookie.setMaxAge(accessTokenExpirationSeconds);

    return accessTokenCookie;
  }

  private JwtAuthResponseDto createUser(RegisterDto registerDto) {
    User user = new User();
    user.setName(registerDto.getName());
    user.setUsername(registerDto.getUsername());
    user.setEmail(registerDto.getEmail());
    user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
    user.setCreatedAt(new Date());

    String roleName = String.valueOf(Roles.ROLE_USER);
    Optional<Role> userRoleOptional = Optional.ofNullable(roleRepository.findByName(roleName));
    Role userRole;

    if (userRoleOptional.isPresent()) {
      userRole = userRoleOptional.get();
    } else {
      userRole = new Role();
      userRole.setName(roleName);
      userRole = roleRepository.save(userRole);
    }

    Set<Role> roles = new HashSet<>();
    roles.add(userRole);
    user.setRoles(roles);

    userRepository.save(user);

    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            registerDto.getUsername(), registerDto.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String accessToken = jwtTokenProvider.generateAccessToken(user, authentication);
    String refreshToken = jwtTokenProvider.generateRefreshToken(user);
    return new JwtAuthResponseDto(accessToken, refreshToken, user.getUsername(), user.getName(), Roles.getHighestRole(user.getRoles()), user.getEmail(), user.getId(), user.getBio(), user.isVerified());

  }

  @Override
  public JwtAuthResponseDto verifyRegister(VerifyRegisterDto verifyRegisterDto) {
    try {
      Claims registerClaims = jwtTokenProvider.validateToken(verifyRegisterDto.getVerifyToken());
      return createUser(
              new RegisterDto(
                      registerClaims.get("username").toString(),
                      registerClaims.get("password").toString(),
                      registerClaims.get("name").toString(),
                      registerClaims.get("email").toString()
              ));
    } catch (JwtException e) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid token");

    }
  }

  @Override
  public JwtAuthResponseDto login(LoginDto loginDto, HttpServletResponse response) {
    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            loginDto.getUsernameOrEmail(),
            loginDto.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    User user = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(),
            loginDto.getUsernameOrEmail()).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));

    String accessToken = jwtTokenProvider.generateAccessToken(user, authentication);
    String refreshToken = jwtTokenProvider.generateRefreshToken(user);


    response.addCookie(createAccessCookie(accessToken));

    Cookie refreshTokenCookie = new Cookie("r_t", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(false);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(refreshTokenExpirationSeconds);
    response.addCookie(refreshTokenCookie);

    return new JwtAuthResponseDto(accessToken, refreshToken, user.getUsername(), user.getName(), Roles.getHighestRole(user.getRoles()), user.getEmail(), user.getId(), user.getBio(), user.isVerified());
  }

  @Override
  public boolean logout(HttpServletResponse response) {
    Cookie refreshTokenCookie = new Cookie("r_t", null);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(false);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(0);
    response.addCookie(refreshTokenCookie);

    Cookie accessTokenCookie = new Cookie("a_t", null);
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setSecure(false);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(0);
    response.addCookie(accessTokenCookie);

    return true;
  }
}
