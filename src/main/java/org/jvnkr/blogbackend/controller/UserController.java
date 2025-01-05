package org.jvnkr.blogbackend.controller;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.UserEditAccountDto;
import org.jvnkr.blogbackend.dto.UserEditProfileDto;
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
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(userService.followUser(viewerId, username));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @GetMapping("/unfollow/{username}")
  public ResponseEntity<Boolean> unfollowUser(@PathVariable String username, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(userService.unfollowUser(viewerId, username));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @GetMapping("/{username}")
  public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String username, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.OK).body(userService.getUserProfile(username, viewerId));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @PostMapping("/edit")
  public ResponseEntity<UserProfileDto> editUserProfile(@RequestBody UserEditProfileDto userEditProfileDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.editProfile(viewerId, userEditProfileDto));
  }

  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @PostMapping("/edit/account")
  public ResponseEntity<UserResponseDto> editUserAccount(@RequestBody UserEditAccountDto userEditAccountDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    UUID viewerId = customUserDetails != null ? customUserDetails.getUserId() : null;
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.editAccount(viewerId, userEditAccountDto));
  }

}
