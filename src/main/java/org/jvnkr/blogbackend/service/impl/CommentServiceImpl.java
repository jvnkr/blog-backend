package org.jvnkr.blogbackend.service.impl;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.CommentDto;
import org.jvnkr.blogbackend.entity.Comment;
import org.jvnkr.blogbackend.entity.Post;
import org.jvnkr.blogbackend.entity.User;
import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.mapper.CommentMapper;
import org.jvnkr.blogbackend.repository.CommentRepository;
import org.jvnkr.blogbackend.repository.PostRepository;
import org.jvnkr.blogbackend.repository.UserRepository;
import org.jvnkr.blogbackend.service.CommentService;
import org.jvnkr.blogbackend.utils.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
  private CommentRepository commentRepository;
  private PostRepository postRepository;
  private UserRepository userRepository;

  @Transactional
  @Override
  public CommentDto createComment(UUID postId, UUID userId, String commentText) {
    if (postId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: post id is required");
    }

    if (userId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: user id is required");
    }

    Post post = postRepository.findById(postId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Post not found"));
    User user = userRepository.findById(userId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));

    if (commentText == null || commentText.isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: comment text is required");
    }
    commentText = commentText.trim();

    Comment comment = new Comment();
    comment.setText(commentText);
    comment.setCreatedAt(new Date());
    comment.setPost(post);
    comment.setUser(user);

    post.addComment(comment);
    commentRepository.save(comment);
    return CommentMapper.toCommentDto(comment, user);
  }

  @Override
  public boolean likeComment(UUID commentId, UUID userId) {
    if (commentId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: comment id is required");
    }
    if (userId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: user id is required");
    }
    User user = userRepository.findById(userId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));
    Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Comment not found"));

    if (comment.getLikedBy().contains(user)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Comment already liked by this user.");
    }
    comment.addLike(user);
    commentRepository.save(comment);
    return true;
  }


  @Override
  public boolean unlikeComment(UUID commentId, UUID userId) {
    if (commentId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: comment id is required");
    }
    if (userId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: user id is required");
    }
    User user = userRepository.findById(userId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));
    Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Comment not found"));

    if (!comment.getLikedBy().contains(user)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "User has not liked this comment.");
    }
    comment.removeLike(user);
    commentRepository.save(comment);
    return true;
  }

  @Transactional
  @Override
  public String removeComment(UUID commentId, UUID userId) {
    if (commentId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: comment id is required");
    }

    if (userId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: user id is required");
    }


    Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Comment not found"));

    if (comment.getParentComment() != null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid comment id");
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));

    if (!user.getId().equals(comment.getUser().getId())) {
      throw new APIException(HttpStatus.FORBIDDEN, "Comment does not beUUID to this user.");
    }

    Post post = comment.getPost();
    post.removeComment(comment);

    // Orphan removal will take care of deleting child comments (replies)
    commentRepository.delete(comment);

    return "Comment removed successfully.";
  }

  @Transactional
  // Transactional keeps the integrity of data and ensures that the operations are atomic, consistent, isolated, and durable (ACID).
  @Override
  public CommentDto addReply(UUID rootCommentId, UUID commentId, UUID viewerId, String replyText) {
    if (rootCommentId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: parent comment id is required");
    }
    if (commentId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: comment id is required");
    }
    if (viewerId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: viewer id is required");
    }
    if (replyText == null || replyText.isEmpty()) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: reply text is required");
    }
    Comment rootComment = commentRepository.findById(rootCommentId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Parent Comment not found"));
    Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Comment not found"));
    User viewer = userRepository.findById(viewerId).orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));

    replyText = replyText.trim();

    Comment replyComment = new Comment();
    replyComment.setText(replyText);
    replyComment.setCreatedAt(new Date());
    replyComment.setPost(comment.getPost());
    replyComment.setUser(viewer);
    replyComment.setRepliesToComment(comment);

    rootComment.addReply(replyComment);
    commentRepository.save(replyComment);
    return CommentMapper.toCommentDto(replyComment, viewer);
  }

  @Transactional
  @Override
  public String removeReply(UUID replyId, UUID userId) {
    if (replyId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: reply id is required");
    }
    if (userId == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Invalid payload: user id is required");
    }

    Comment reply = commentRepository.findById(replyId)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Reply not found"));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));

    if (!user.getId().equals(reply.getUser().getId())) {
      throw new APIException(HttpStatus.FORBIDDEN, "Reply does not beUUID to this user.");
    }

    Comment parentComment = reply.getParentComment();
    parentComment.removeReply(reply);
    commentRepository.save(parentComment);

    return "Reply removed successfully.";
  }

  @Override
  public List<CommentDto> getBatchOfComments(UUID postId, int pageNumber, int batchSize, UUID viewerId) {
    Pagination.validate(pageNumber, batchSize, viewerId, userRepository);

    User viewer;
    if (viewerId != null) viewer = userRepository.findById(viewerId).orElse(null);
    else viewer = null;

    boolean existsPost = postRepository.existsById(postId);
    if (!existsPost) {
      throw new APIException(HttpStatus.NOT_FOUND, "Invalid payload: post not found");
    }

    Pageable pageable = PageRequest.of(pageNumber, batchSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Comment> commentPage = commentRepository.findByPostIdAndParentCommentIdIsNull(postId, pageable);

    List<Comment> comments = commentPage.getContent();

    return comments.stream().map(comment -> CommentMapper.toCommentDto(comment, viewer)).collect(Collectors.toList());
  }

  @Override
  public List<CommentDto> getBatchOfReplies(UUID rootCommentId, int pageNumber, int batchSize, UUID viewerId) {
    Pagination.validate(pageNumber, batchSize, viewerId, userRepository);

    User viewer;
    if (viewerId != null) viewer = userRepository.findById(viewerId).orElse(null);
    else viewer = null;

    Comment comment = commentRepository.findById(rootCommentId).orElse(null);
    if (comment == null) {
      throw new APIException(HttpStatus.NOT_FOUND, "Invalid payload: comment not found");
    }

    if (comment.getParentComment() != null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Cannot fetch replies from a reply");

    }

    // Direction.ASC - ascending
    // Direction.DESC - descending
    Pageable pageable = PageRequest.of(pageNumber, batchSize, Sort.by(Sort.Direction.ASC, "createdAt"));
    Page<Comment> replyPage = commentRepository.findByParentCommentId(rootCommentId, pageable);

    List<Comment> replies = replyPage.getContent();

    return replies.stream().map((com -> CommentMapper.toCommentDto(com, viewer))).collect(Collectors.toList());
  }


}