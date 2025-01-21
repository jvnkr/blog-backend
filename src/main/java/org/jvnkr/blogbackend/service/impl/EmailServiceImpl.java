package org.jvnkr.blogbackend.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.jvnkr.blogbackend.dto.RegisterDto;
import org.jvnkr.blogbackend.security.JwtTokenProvider;
import org.jvnkr.blogbackend.service.EmailService;
import org.jvnkr.blogbackend.utils.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
  private final JwtTokenProvider jwtTokenProvider;

  @Value("${app.resend-api-key}")
  private String resendKey;

  private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

  @Autowired
  public EmailServiceImpl(
          JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void sendVerificationEmail(String recipientEmail, RegisterDto registerDto) {
    try {
      Resend resend = new Resend(resendKey);


      String token = jwtTokenProvider.generateVerifyToken(registerDto);

      String encodedToken;
      try {
        encodedToken = Encode.encodeToken(token);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      String verificationLink = "http://localhost:8080/api/v1/email/verify?t=" + encodedToken;

      String from = "Blogify <hello@jvnkr.com>";
      String subject = "Verify your email address";

      String htmlContent = "<p>You know the drill. Click the link below to verify your email address.</p>"
              + "<p>Note: If you don't click the link within 24 hours it will expire.</p>"
              + "<p><a href=\"" + verificationLink + "\"><strong>Verify email address →</strong></a></p>"
              + "<p>Stay bloggin', @blogify</p>"
              + "<p>If you have any trouble with the button, you can copy and paste the link below into your browser:</p>"
              + "<p><a href=\"" + verificationLink + "\">" + verificationLink + "</a></p>"
              + "<p>© 2024 Blogify, LLC.<br>"
              + "<p>81000 Podgorica, Montenegro</p>";

      CreateEmailOptions params = CreateEmailOptions.builder()
              .from(from)
              .to(recipientEmail.trim())
              .subject(subject)
              .html(htmlContent)
              .build();

      resend.emails().send(params);
    } catch (ResendException e) {
      logger.error("Error while sending email: {}", e.getMessage());
    }
  }

}
