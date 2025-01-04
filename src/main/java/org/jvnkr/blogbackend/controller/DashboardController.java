package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.DashboardDto;
import org.jvnkr.blogbackend.security.CustomUserDetails;
import org.jvnkr.blogbackend.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
  private final DashboardService dashboardService;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(dashboardService.getDashboard(viewerId));
  }
}
