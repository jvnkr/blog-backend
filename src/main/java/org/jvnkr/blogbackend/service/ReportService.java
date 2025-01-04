package org.jvnkr.blogbackend.service;

import org.jvnkr.blogbackend.dto.ReportsDto;

import java.util.List;
import java.util.UUID;

public interface ReportService {
  boolean submitReport(UUID viewerId, UUID postId, List<String> reasons, String details);

  ReportsDto getBatchOfReports(int pageNumber, int batchSize);

  boolean updateReportStatus(ReportStatus newStatus, UUID reportId);
}
