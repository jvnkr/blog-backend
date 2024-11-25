package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.UserResponseDto;
import org.jvnkr.blogbackend.security.CustomUserDetails;
import org.jvnkr.blogbackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private UserService userService;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping()
  public ResponseEntity<List<UserResponseDto>> getAllUsers() {
    return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @PostMapping("/follow/{followingId}")
  public ResponseEntity<Boolean> followUser(@PathVariable UUID followingId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.followUser(customUserDetails.getUserId(), followingId));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @PostMapping("/unfollow/{unfollowingId}")
  public ResponseEntity<Boolean> unfollowUser(@PathVariable UUID unfollowingId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.unfollowUser(customUserDetails.getUserId(), unfollowingId));
  }

}
