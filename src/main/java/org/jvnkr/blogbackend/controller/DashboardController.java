package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.DashboardDto;
import org.jvnkr.blogbackend.dto.PageNumberDto;
import org.jvnkr.blogbackend.dto.PostDto;
import org.jvnkr.blogbackend.security.CustomUserDetails;
import org.jvnkr.blogbackend.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/posts")
  public ResponseEntity<List<PostDto>> getDashboardPosts(@RequestBody PageNumberDto pageNumberDto) {
    int BATCH_SIZE = 10;
    return ResponseEntity.status(HttpStatus.OK).body(dashboardService.getBatchOfDashoardPosts(pageNumberDto.getPageNumber(), BATCH_SIZE));
  }
}