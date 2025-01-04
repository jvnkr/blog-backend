package org.jvnkr.blogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jvnkr.blogbackend.service.ReportStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reports")
public class Report {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "details")
  private String details;

  @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ReportReason> reasons = new ArrayList<>();

  @Column(name = "status")
  @Enumerated(EnumType.STRING) // Maps the enum to a String in the database
  private ReportStatus status = ReportStatus.PENDING;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
  private Date createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;
}