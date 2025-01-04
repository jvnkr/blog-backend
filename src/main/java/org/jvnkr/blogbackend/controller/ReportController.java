package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.GetReportsDto;
import org.jvnkr.blogbackend.dto.ReportDto;
import org.jvnkr.blogbackend.dto.ReportsDto;
import org.jvnkr.blogbackend.dto.UpdateReportStatusDto;
import org.jvnkr.blogbackend.security.CustomUserDetails;
import org.jvnkr.blogbackend.service.ReportService;
import org.jvnkr.blogbackend.service.ReportStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/report")
public class ReportController {
  private final ReportService reportService;

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping
  public ResponseEntity<Boolean> submitReport(@RequestBody ReportDto reportDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.CREATED).body(reportService.submitReport(viewerId, reportDto.getPostId(), reportDto.getReasons(), reportDto.getDetails()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/batch")
  public ResponseEntity<ReportsDto> getReports(@RequestBody GetReportsDto getReportsDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(reportService.getBatchOfReports(getReportsDto.getPageNumber(), getReportsDto.getBatchSize()));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping
  public ResponseEntity<Boolean> updateReportStatus(@RequestBody UpdateReportStatusDto updateReportStatusDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(reportService.updateReportStatus(ReportStatus.fromString(updateReportStatusDto.getNewStatus()), updateReportStatusDto.getReportId()));
  }
}
