package org.jvnkr.blogbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jvnkr.blogbackend.service.ReportStatus;
import org.jvnkr.blogbackend.service.Reports;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetReportDto {
  private UUID id;
  private String postTitle;
  private UUID postId;
  private List<Reports> reasons;
  private String details;
  private ReportStatus status;
  private Date createdAt;
  private UserInfoDto reportedBy;
}
