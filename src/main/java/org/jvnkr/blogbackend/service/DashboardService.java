package org.jvnkr.blogbackend.service;

import org.jvnkr.blogbackend.dto.DashboardDto;

import java.util.UUID;

public interface DashboardService {
  DashboardDto getDashboard(UUID viewerId);
}
