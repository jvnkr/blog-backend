package org.jvnkr.blogbackend.service;


import org.jvnkr.blogbackend.dto.CommentDto;

import java.util.List;
import java.util.UUID;

public interface CommentService {
  String createComment(UUID postId, UUID userId, String commentText);

  String removeComment(UUID commentId, UUID userId);

  String addReply(UUID parentCommentId, UUID userId, String replyText);

  String removeReply(UUID replyId, UUID userId);

  List<CommentDto> getBatchOfComments(UUID postId, int pageNumber, int batchSize);

  List<CommentDto> getBatchOfReplies(UUID commentId, int pageNumber, int batchSize);
}