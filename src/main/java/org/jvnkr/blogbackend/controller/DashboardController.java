package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.DashboardDto;
import org.jvnkr.blogbackend.dto.GetDashboardDto;
import org.jvnkr.blogbackend.dto.PageNumberDto;
import org.jvnkr.blogbackend.dto.PostDto;
import org.jvnkr.blogbackend.security.CustomUserDetails;
import org.jvnkr.blogbackend.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
  private final DashboardService dashboardService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<DashboardDto> getDashboard(@RequestBody GetDashboardDto getDashboardDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(dashboardService.getDashboard(getDashboardDto.getYear(), viewerId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/posts")
  public ResponseEntity<List<PostDto>> getDashboardPosts(@RequestBody PageNumberDto pageNumberDto) {
    int BATCH_SIZE = 10;
    return ResponseEntity.status(HttpStatus.OK).body(dashboardService.getBatchOfDashoardPosts(pageNumberDto.getPageNumber(), BATCH_SIZE));
  }
}