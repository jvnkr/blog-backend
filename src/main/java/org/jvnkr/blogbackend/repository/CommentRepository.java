package org.jvnkr.blogbackend.repository;

import org.jvnkr.blogbackend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  Page<Comment> findByPostIdAndParentCommentIdIsNull(UUID post_id, Pageable pageable);

  Page<Comment> findByParentCommentId(UUID parentComment_id, Pageable pageable);

}
