package org.jvnkr.blogbackend.repository;

import jakarta.persistence.LockModeType;
import org.jvnkr.blogbackend.dto.DashboardChartDto;
import org.jvnkr.blogbackend.entity.Post;
import org.jvnkr.blogbackend.entity.User;
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

  @Query(value = "SELECT EXTRACT(YEAR FROM created_at) FROM posts ORDER BY created_at ASC LIMIT 1", nativeQuery = true)
  Optional<Integer> findYearOfOldestPost();

  @Query("SELECT p FROM Post p " +
          "WHERE p.user.id IN (" +
          "SELECT f.id FROM User u JOIN u.following f WHERE u.id = :userId)")
  Page<Post> findPostsByFollowedUsers(@Param("userId") UUID userId, Pageable pageable);

  @Query("""
          SELECT u
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
  List<User> findTopUsersByPostsAndEngagement(Pageable pageable);

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

  /**
   * Performs a fuzzy search on the posts table using PostgreSQL's pg_trgm extension.
   * This method searches for posts whose title or description is similar to the provided query string.
   * <p>
   * The similarity is calculated using the `similarity` function provided by the pg_trgm extension,
   * and the `%` operator is used to filter results based on trigram similarity.
   * <p>
   * The results are ordered by a calculated relevance score, which is the greatest similarity
   * between the title and description fields.
   * <p>
   * Note:
   * - Ensure the pg_trgm extension is enabled in your PostgreSQL database.
   * - Consider creating GIN or GiST indexes on the title and description columns to improve performance.
   *
   * @param query The search query string to find similar titles or descriptions.
   * @return A list of Post entities that match the fuzzy search criteria, ordered by relevance.
   */
  @Query(value = """
          SELECT *, GREATEST(similarity(title, :query), similarity(description, :query)) AS relevance
            FROM posts
            WHERE (similarity(title, :query) >= :sensitivity OR similarity(description, :query) >= :sensitivity)
            ORDER BY relevance DESC
            LIMIT :limit OFFSET :offset;
          """, nativeQuery = true)
  List<Post> fuzzySearchPosts(@Param("query") String query, @Param("limit") int limit, @Param("offset") int offset, @Param("sensitivity") float sensitivity);

  @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.createdAt DESC")
  List<Post> findByTitleOrDescContainingIgnoreCase(@Param("query") String query, Pageable pageable);

}