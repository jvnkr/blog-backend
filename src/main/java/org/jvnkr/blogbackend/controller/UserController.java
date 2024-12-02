package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.UserProfileDto;
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
  @GetMapping("/follow/{username}")
  public ResponseEntity<Boolean> followUser(@PathVariable String username, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.followUser(customUserDetails.getUserId(), username));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @GetMapping("/unfollow/{username}")
  public ResponseEntity<Boolean> unfollowUser(@PathVariable String username, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.unfollowUser(customUserDetails.getUserId(), username));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @GetMapping("/{username}")
  public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String username) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.getUserProfile(username));
  }

}
