package org.jvnkr.blogbackend.service;


import org.jvnkr.blogbackend.dto.CommentDto;

import java.util.List;
import java.util.UUID;

public interface CommentService {
  CommentDto createComment(UUID postId, UUID userId, String commentText);

  boolean likeComment(UUID commentId, UUID userId);

  boolean unlikeComment(UUID commentId, UUID userId);

  String removeComment(UUID commentId, UUID userId);

  CommentDto addReply(UUID parentCommentId, UUID commentId, UUID userId, String replyText);

  String removeReply(UUID replyId, UUID userId);

  List<CommentDto> getBatchOfComments(UUID postId, int pageNumber, int batchSize, UUID viewerId);

  List<CommentDto> getBatchOfReplies(UUID commentId, int pageNumber, int batchSize, UUID viewerId);
}