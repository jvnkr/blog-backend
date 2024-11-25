package org.jvnkr.blogbackend.repository;

import org.jvnkr.blogbackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
  Page<Post> findByUserId(UUID user_id, Pageable pageable);
}
