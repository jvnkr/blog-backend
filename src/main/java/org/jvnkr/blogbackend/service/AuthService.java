package org.jvnkr.blogbackend.service;

import jakarta.servlet.http.HttpServletResponse;
import org.jvnkr.blogbackend.dto.*;

public interface AuthService {
  JwtAuthResponseDto register(RegisterDto registerDto);

  JwtAuthResponseDto login(LoginDto loginDto, HttpServletResponse response);

  SessionTokenDto validateSession(ValidateTokensDto validateTokensDto, HttpServletResponse response);

  JwtAuthResponseDto verifyRegister(VerifyRegisterDto verifyRegisterDto);
}
