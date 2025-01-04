package org.jvnkr.blogbackend.service;

import lombok.Getter;

@Getter
public enum ReportStatus {
  PENDING("PENDING"),
  RESOLVED("RESOLVED"),
  DISMISSED("DISMISSED");

  private final String reportStatus;

  ReportStatus(String reportStatus) {
    this.reportStatus = reportStatus;
  }

  public static ReportStatus fromString(String status) {
    for (ReportStatus rep : values()) {
      if (rep.getReportStatus().equalsIgnoreCase(status)) {
        return rep;
      }
    }
    throw new IllegalArgumentException("Unknown report reason: " + status);
  }
}
