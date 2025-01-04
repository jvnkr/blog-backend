package org.jvnkr.blogbackend.mapper;

import org.jvnkr.blogbackend.dto.GetReportDto;
import org.jvnkr.blogbackend.entity.Report;
import org.jvnkr.blogbackend.service.Reports;


public class ReportMapper {
  public static GetReportDto toGetReportDto(Report report) {
    return new GetReportDto(
            report.getId(),
            report.getPost().getTitle(),
            report.getPost().getId(),
            Reports.toList(report.getReasons()),
            report.getDetails(),
            report.getStatus(),
            report.getCreatedAt(),
            UserMapper.toUserInfoDto(report.getUser())
    );
  }

}
