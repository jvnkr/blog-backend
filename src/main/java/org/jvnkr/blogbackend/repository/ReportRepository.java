package org.jvnkr.blogbackend.repository;

import org.jvnkr.blogbackend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
  
}
