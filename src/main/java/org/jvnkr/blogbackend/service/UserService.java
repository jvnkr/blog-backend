package org.jvnkr.blogbackend.service;

import org.jvnkr.blogbackend.dto.UserProfileDto;
import org.jvnkr.blogbackend.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
  List<UserResponseDto> getAllUsers();

  boolean followUser(UUID viewerId, String username);

  boolean unfollowUser(UUID viewerId, String username);

  UserProfileDto getUserProfile(String username);
}
