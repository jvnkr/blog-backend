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
  public ResponseEntity<String> createComment(@PathVariable UUID postId, @RequestBody CommentDto commentDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(postId, customUserDetails.getUserId(), commentDto.getText()));
  }


  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<String> removeComment(@PathVariable UUID commentId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.removeComment(commentId, customUserDetails.getUserId()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping("/reply/{commentId}")
  public ResponseEntity<String> addReply(@PathVariable UUID commentId, @RequestBody CommentDto replyDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addReply(commentId, customUserDetails.getUserId(), replyDto.getText()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @DeleteMapping("/reply/{replyId}")
  public ResponseEntity<String> removeReply(@PathVariable UUID replyId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED).body(commentService.removeReply(replyId, customUserDetails.getUserId()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @GetMapping("/batch/{postId}")
  public ResponseEntity<List<CommentDto>> getCommentsByPostId(@PathVariable UUID postId, @RequestBody PageNumberDto pageNumberDto) {
    final int BATCH_SIZE = 10;
    return ResponseEntity.status(HttpStatus.OK).body(commentService.getBatchOfComments(postId, pageNumberDto.getPageNumber(), BATCH_SIZE));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @GetMapping("/reply/batch/{commentId}")
  public ResponseEntity<List<CommentDto>> getRepliesByCommentId(@PathVariable UUID commentId, @RequestBody PageNumberDto pageNumberDto) {
    final int BATCH_SIZE = 10;
    return ResponseEntity.status(HttpStatus.OK).body(commentService.getBatchOfReplies(commentId, pageNumberDto.getPageNumber(), BATCH_SIZE));
  }

}
