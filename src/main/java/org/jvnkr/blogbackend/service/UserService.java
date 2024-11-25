package org.jvnkr.blogbackend.service;

import org.jvnkr.blogbackend.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
  List<UserResponseDto> getAllUsers();

  boolean followUser(UUID userId, UUID followerId);

  boolean unfollowUser(UUID userId, UUID followerId);
}
