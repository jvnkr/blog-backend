package org.jvnkr.blogbackend.service;

import org.jvnkr.blogbackend.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {
  List<UserResponseDto> getAllUsers();

  boolean followUser(UUID viewerId, String username);

  boolean unfollowUser(UUID viewerId, String username);

  UserProfileDto getUserProfile(String username, UUID viewerId);

  UserProfileDto editProfile(UUID viewerId, UserEditProfileDto newProfile);

  UserResponseDto editAccount(UUID viewerId, UserEditAccountDto newProfile);

  List<UserInfoDto> searchUsers(String query);

  List<UserInfoDto> searchUsersPaginated(String query, int pageNumber, int batchSize);
}
