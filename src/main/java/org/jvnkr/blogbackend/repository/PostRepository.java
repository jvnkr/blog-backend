package org.jvnkr.blogbackend.repository;

import jakarta.persistence.LockModeType;
import org.jvnkr.blogbackend.dto.DashboardChartDto;
import org.jvnkr.blogbackend.dto.PostAuthorDto;
import org.jvnkr.blogbackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
  // Mitigating race conditions
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Post p WHERE p.id = :postId")
  Optional<Post> findByIdWithLock(@Param("postId") UUID postId);

  Page<Post> findByUserId(UUID userId, Pageable pageable);

  @Query("SELECT p FROM Post p " +
          "WHERE p.user.id IN (" +
          "SELECT f.id FROM User u JOIN u.following f WHERE u.id = :userId)")
  Page<Post> findPostsByFollowedUsers(@Param("userId") UUID userId, Pageable pageable);

  @Query("""
          SELECT new org.jvnkr.blogbackend.dto.PostAuthorDto(
              u.id,
              u.name,
              u.username,
              u.verified
          )
          FROM User u
          LEFT JOIN Post p ON p.user.id = u.id
          WHERE u.deleted = false
          GROUP BY u.id, u.name, u.username, u.verified
          HAVING COUNT(p) > 0
          ORDER BY (
              COUNT(p) +
              COALESCE(SUM((SELECT COUNT(l) FROM p.likedBy l)), 0) +
              COALESCE(SUM((SELECT COUNT(c) FROM p.comments c)), 0)
          ) DESC
          """)
  List<PostAuthorDto> findTopUsersByPostsAndEngagement(Pageable pageable);

  @Query("""
          SELECT p
          FROM Post p
          LEFT JOIN p.likedBy l
          LEFT JOIN p.comments c
          GROUP BY p.id
          ORDER BY (COUNT(l) + COUNT(c)) DESC
          """)
  List<Post> findTopPerformingPosts(Pageable pageable);

  @Query("""
          SELECT new org.jvnkr.blogbackend.dto.DashboardChartDto(
              TO_CHAR(p.createdAt, 'Mon'),
              COUNT(p)
          )
          FROM Post p
          WHERE p.createdAt >= :startOfYear AND p.createdAt <= :endOfYear
          GROUP BY TO_CHAR(p.createdAt, 'Mon'), EXTRACT(YEAR FROM p.createdAt), EXTRACT(MONTH FROM p.createdAt)
          ORDER BY EXTRACT(YEAR FROM p.createdAt), EXTRACT(MONTH FROM p.createdAt)
          """)
  List<DashboardChartDto> findPostCountsForCurrentYear(
          @Param("startOfYear") LocalDateTime startOfYear,
          @Param("endOfYear") LocalDateTime endOfYear
  );
}