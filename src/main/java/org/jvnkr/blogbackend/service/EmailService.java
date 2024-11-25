package org.jvnkr.blogbackend.service;

import org.jvnkr.blogbackend.dto.RegisterDto;

public interface EmailService {
  void sendVerificationEmail(String recipientEmail, RegisterDto registerDto);
}
