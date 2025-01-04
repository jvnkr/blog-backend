package org.jvnkr.blogbackend.service;

import lombok.Getter;
import org.jvnkr.blogbackend.entity.ReportReason;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum Reports {
  INAPPROPRIATE("Inappropriate"),
  SPAM("Spam"),
  HARASSMENT("Harassment"),
  MISINFORMATION("Misinformation"),
  OTHER("Other");

  private final String reportName;

  Reports(String reportName) {
    this.reportName = reportName;
  }

  public static Reports fromName(String reportName) {
    for (Reports rep : values()) {
      if (rep.getReportName().equalsIgnoreCase(reportName)) {
        return rep;
      }
    }
    throw new IllegalArgumentException("Unknown report reason: " + reportName);
  }

  public static List<Reports> toList(List<ReportReason> reports) {
    ArrayList<Reports> a = new ArrayList<>();
    for (ReportReason rep : reports) {
      a.add(rep.getReason());
    }
    return a;
  }

}
