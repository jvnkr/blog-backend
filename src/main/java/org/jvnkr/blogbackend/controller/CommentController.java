package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.CommentDto;
import org.jvnkr.blogbackend.dto.PageNumberDto;
import org.jvnkr.blogbackend.security.CustomUserDetails;
import org.jvnkr.blogbackend.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
  private final CommentService commentService;

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping("/{postId}")
  public ResponseEntity<CommentDto> createComment(@PathVariable UUID postId, @RequestBody CommentDto commentDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(postId, customUserDetails.getUserId(), commentDto.getText()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @GetMapping("/like/{commentId}")
  public ResponseEntity<Boolean> likeComment(@PathVariable UUID commentId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.likeComment(commentId, customUserDetails.getUserId()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @DeleteMapping("/like/{commentId}")
  public ResponseEntity<Boolean> unlikeComment(@PathVariable UUID commentId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.unlikeComment(commentId, customUserDetails.getUserId()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<String> removeComment(@PathVariable UUID commentId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.removeComment(commentId, customUserDetails.getUserId()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping("/reply/{commentId}")
  public ResponseEntity<CommentDto> addReply(@PathVariable UUID commentId, @RequestBody CommentDto replyDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addReply(replyDto.getRootId(), commentId, customUserDetails.getUserId(), replyDto.getText()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @DeleteMapping("/reply/{replyId}")
  public ResponseEntity<String> removeReply(@PathVariable UUID replyId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.removeReply(replyId, customUserDetails.getUserId()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping("/batch/{postId}")
  public ResponseEntity<List<CommentDto>> getCommentsByPostId(@PathVariable UUID postId, @RequestBody PageNumberDto pageNumberDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    final int BATCH_SIZE = 10;
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(commentService.getBatchOfComments(postId, pageNumberDto.getPageNumber(), BATCH_SIZE, viewerId));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping("/reply/batch/{commentId}")
  public ResponseEntity<List<CommentDto>> getRepliesByCommentId(@PathVariable UUID commentId, @RequestBody PageNumberDto pageNumberDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    final int BATCH_SIZE = 3;
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(commentService.getBatchOfReplies(commentId, pageNumberDto.getPageNumber(), BATCH_SIZE, viewerId));
  }

}
