package org.jvnkr.blogbackend.service.impl;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.GetReportDto;
import org.jvnkr.blogbackend.dto.ReportsDto;
import org.jvnkr.blogbackend.entity.Post;
import org.jvnkr.blogbackend.entity.Report;
import org.jvnkr.blogbackend.entity.ReportReason;
import org.jvnkr.blogbackend.entity.User;
import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.mapper.ReportMapper;
import org.jvnkr.blogbackend.repository.PostRepository;
import org.jvnkr.blogbackend.repository.ReportRepository;
import org.jvnkr.blogbackend.repository.UserRepository;
import org.jvnkr.blogbackend.service.ReportService;
import org.jvnkr.blogbackend.service.ReportStatus;
import org.jvnkr.blogbackend.service.Reports;
import org.jvnkr.blogbackend.utils.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReportServiceImpl implements ReportService {
  private final UserRepository userRepository;
  private final ReportRepository reportRepository;
  private final PostRepository postRepository;

  @Override
  @Transactional
  public boolean submitReport(UUID viewerId, UUID postId, List<String> reasons, String details) {
    if (reasons == null || reasons.isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Reasons must not be empty");
    }
    if (viewerId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Viewer ID must not be null");
    }

    User viewer = userRepository.findById(viewerId).orElse(null);
    if (viewer == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Viewer not found");
    }
    Post post = postRepository.findByIdWithLock(postId).orElse(null);

    if (post == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Post not found");
    }

    if (post.getUser().getId().equals(viewerId)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You cannot report your own post");
    }

    try {
      Report report = new Report();

      for (String reason : reasons) {
        if (Reports.fromName(reason).equals(Reports.OTHER) && details.trim().isEmpty()) {
          throw new APIException(HttpStatus.BAD_REQUEST, "Other reason must have a description");
        }
        ReportReason reportReason = new ReportReason();
        reportReason.setReport(report);
        reportReason.setReason(Reports.fromName(reason));

        report.getReasons().add(reportReason);
      }
      report.setDetails(details.trim());
      report.setCreatedAt(new Date());
      report.setUser(viewer);
      report.setPost(post);
      reportRepository.save(report);
      return true;
    } catch (IllegalArgumentException e) {
      throw new APIException(HttpStatus.BAD_REQUEST, e.getMessage());
    }


  }

  @Override
  public ReportsDto getBatchOfReports(int pageNumber, int batchSize) {
    Pagination.validate(pageNumber, batchSize, null, userRepository);

    Pageable pageable = PageRequest.of(pageNumber, batchSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Report> reportPage = reportRepository.findAll(pageable);
    List<GetReportDto> reports = reportPage.getContent().stream()
            .map(ReportMapper::toGetReportDto)
            .toList();

    long totalReports = reportRepository.count();
    int pages = (int) Math.ceil((double) totalReports / batchSize);

    return new ReportsDto(
            pages,
            reports
    );
  }

  @Override
  public boolean updateReportStatus(ReportStatus newStatus, UUID reportId) {
    if (reportId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Report ID must not be null");
    }
    Report report = reportRepository.findById(reportId).orElse(null);
    if (report == null) {
      throw new APIException(HttpStatus.NOT_FOUND, "Report not found");
    }

    if (report.getStatus().equals(newStatus)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Report status cannot be equal to the current one");
    }

    report.setStatus(newStatus);
    reportRepository.save(report);
    return true;
  }
}
