package org.jvnkr.blogbackend.repository;

import jakarta.persistence.LockModeType;
import org.jvnkr.blogbackend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
  // Methods like findByUsername and findByEmail leverage Spring Data JPA's query derivation mechanism,
  // where the framework parses the method names and constructs queries appropriately.
  Boolean existsByUsername(String username);

  Optional<User> findByUsernameOrEmail(String username, String email);

  Boolean existsByEmail(String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT u FROM User u WHERE u.id = :userId")
  Optional<User> findByIdWithLock(@Param("userId") UUID userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT u FROM User u WHERE u.email = :email OR u.username = :username")
  Optional<User> findByUsernameOrEmailWithLock(@Param("username") String username, @Param("email") String email);

  /**
   * Performs a fuzzy search on the users table using PostgreSQL's pg_trgm extension.
   * This method searches for users whose username or name is similar to the provided query string.
   * <p>
   * The similarity is calculated using the `similarity` function provided by the pg_trgm extension,
   * and the `%` operator is used to filter results based on trigram similarity.
   * <p>
   * The results are ordered by a calculated relevance score, which is the greatest similarity
   * between the username and name fields.
   * <p>
   * Note:
   * - Ensure the pg_trgm extension is enabled in your PostgreSQL database.
   * - Consider creating GIN or GiST indexes on the username and name columns to improve performance.
   *
   * @param query The search query string to find similar usernames or names.
   * @return A list of User entities that match the fuzzy search criteria, ordered by relevance.
   */
  @Query(value = """
          SELECT *, GREATEST(similarity(username, :query), similarity(name, :query)) AS relevance
          FROM users
          WHERE (similarity(username, :query) >= :sensitivity OR similarity(name, :query) >= :sensitivity)
          ORDER BY relevance DESC
          LIMIT :limit OFFSET :offset
          """, nativeQuery = true)
  List<User> fuzzySearchUsers(
          @Param("query") String query,
          @Param("limit") int limit,
          @Param("offset") int offset,
          @Param("sensitivity") float sensitivity);

  @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY u.createdAt DESC")
  List<User> findByUsernameOrNameContainingIgnoreCase(@Param("query") String query, Pageable pageable);

  // We can do these queries for complex needs or performance optimizations
  // There are also JPA Specifications which allow further customization of the querying without writing much SQL
//  @Query("SELECT u FROM User u WHERE u.username = :username")
//  Optional<User> findByUsername(@Param("username") String username);
//
//  @Query("SELECT u FROM User u WHERE u.email = :email")
//  Optional<User> findByEmail(@Param("email") String email);
}

