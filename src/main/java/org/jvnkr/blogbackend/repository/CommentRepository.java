package org.jvnkr.blogbackend.repository;

import jakarta.persistence.LockModeType;
import org.jvnkr.blogbackend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT c FROM Comment c WHERE c.id = :commentId")
  Optional<Comment> findByIdWithLock(@Param("commentId") UUID commentId);


  Page<Comment> findByPostIdAndParentCommentIdIsNull(UUID post_id, Pageable pageable);

  Page<Comment> findByParentCommentId(UUID parentComment_id, Pageable pageable);

  List<Comment> findByParentComment(Comment parentComment);

  List<Comment> findByRepliesToComment(Comment repliesToComment);
}
