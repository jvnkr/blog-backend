package org.jvnkr.blogbackend.service;

import org.jvnkr.blogbackend.dto.DashboardDto;
import org.jvnkr.blogbackend.dto.PostDto;

import java.util.List;
import java.util.UUID;

public interface DashboardService {
  DashboardDto getDashboard(UUID viewerId);

  List<PostDto> getBatchOfDashoardPosts(int pageNumber, int batchSize);
}
