package org.jvnkr.blogbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
  private int postsCount;
  private int usersCount;
  private int reportsCount;
  private int earliestYear;
  private List<TopUserDto> topUsers;
  private List<DashboardChartDto> postsPerMonth;
}
