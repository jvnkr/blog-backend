package org.jvnkr.blogbackend.repository;

import org.jvnkr.blogbackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
  Page<Post> findByUserId(UUID userId, Pageable pageable);

  @Query("SELECT p FROM Post p WHERE p.user IN (" +
          "SELECT u FROM User u JOIN u.following f WHERE f.id = :userId)")
  Page<Post> findPostsByFollowedUsers(@Param("userId") UUID userId, Pageable pageable);
}