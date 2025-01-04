package org.jvnkr.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jvnkr.blogbackend.service.Reports;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "report_reasons")
public class ReportReason {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING) // Maps to String in the database
  @Column(name = "reason", nullable = false) // Stores the specific reason (e.g., SPAM, INAPPROPRIATE)
  private Reports reason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "report_id", nullable = false) // Foreign key pointing to the parent report
  private Report report;
}