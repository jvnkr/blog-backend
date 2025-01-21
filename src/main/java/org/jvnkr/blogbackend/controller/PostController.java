package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.PageNumberDto;
import org.jvnkr.blogbackend.dto.PostDto;
import org.jvnkr.blogbackend.security.CustomUserDetails;
import org.jvnkr.blogbackend.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
  private final PostService postService;
  final int GLOBAL_BATCH_SIZE = 10;

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping
  public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(postService.createPost(postDto.getTitle(), postDto.getDescription(), customUserDetails.getUserId()));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @PostMapping("/search")
  public ResponseEntity<List<PostDto>> searchPostsPaginated(@RequestParam("q") String query, @RequestParam("filter") String filter, @RequestBody PageNumberDto pageNumberDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(postService.searchPosts(query, pageNumberDto.getPageNumber(), filter.equalsIgnoreCase("users") ? 3 : GLOBAL_BATCH_SIZE, viewerId));
  }

  @GetMapping
  public ResponseEntity<List<PostDto>> getAllPosts() {
    return ResponseEntity.status(HttpStatus.OK).body(postService.getAllPosts());
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @GetMapping("/{postId}")
  public ResponseEntity<PostDto> getPostById(@PathVariable UUID postId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(postService.getPostById(postId, viewerId));
  }

  @PostMapping("/batch")
  public ResponseEntity<List<PostDto>> getAllBatchedPosts(@RequestBody PageNumberDto pageNumberDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(postService.getBatchOfAllPosts(pageNumberDto.getPageNumber(), GLOBAL_BATCH_SIZE, viewerId));
  }


  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping("/batch/following")
  public ResponseEntity<List<PostDto>> getAllFollowingBatchedPosts(@RequestBody PageNumberDto pageNumberDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(postService.getBatchOfAllFollowingPosts(pageNumberDto.getPageNumber(), GLOBAL_BATCH_SIZE, viewerId));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PostMapping("/batch/{username}")
  public ResponseEntity<List<PostDto>> getUserBatchedPosts(@PathVariable String username, @RequestBody PageNumberDto pageNumberDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(postService.getBatchOfUserPosts(username, pageNumberDto.getPageNumber(), GLOBAL_BATCH_SIZE, viewerId));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @PutMapping("/{postId}")
  public ResponseEntity<String> updatePost(@PathVariable UUID postId, @RequestBody PostDto postDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.OK).body(postService.updatePost(postId, customUserDetails.getUserId(), postDto.getTitle(), postDto.getDescription()));
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @DeleteMapping("/{postId}")
  public ResponseEntity<Void> deletePost(@PathVariable UUID postId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    postService.deletePost(postId, customUserDetails.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(null);
  }

  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @GetMapping("/like/{postId}")
  public ResponseEntity<Boolean> likePost(@PathVariable UUID postId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.OK).body(postService.likePost(postId, customUserDetails.getUserId()));
  }


  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  @DeleteMapping("/like/{postId}")
  public ResponseEntity<Boolean> unlikePost(@PathVariable UUID postId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.OK).body(postService.unlikePost(postId, customUserDetails.getUserId()));
  }

}