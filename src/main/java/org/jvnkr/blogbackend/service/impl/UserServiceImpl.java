package org.jvnkr.blogbackend.service.impl;

import lombok.AllArgsConstructor;
import org.jvnkr.blogbackend.dto.UserEditProfileDto;
import org.jvnkr.blogbackend.dto.UserProfileDto;
import org.jvnkr.blogbackend.dto.UserResponseDto;
import org.jvnkr.blogbackend.entity.User;
import org.jvnkr.blogbackend.exception.APIException;
import org.jvnkr.blogbackend.mapper.UserMapper;
import org.jvnkr.blogbackend.repository.UserRepository;
import org.jvnkr.blogbackend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  @Override
  public List<UserResponseDto> getAllUsers() {
    List<User> users = userRepository.findAll();
    return users.stream()
            .map(UserMapper::toUserResponseDto)
            .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public boolean followUser(UUID viewerId, String username) {
    // Retrieve the users with locking for concurrency control
    User user = userRepository.findByIdWithLock(viewerId)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));

    User follower = userRepository.findByUsernameOrEmailWithLock(username, null)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Follower not found"));

    // Prevent self-following
    if (user.getUsername().equals(username)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
    }

    // Add follower only if not already following
    if (!follower.getFollowers().contains(user)) {
      user.getFollowing().add(follower);
      follower.getFollowers().add(user); // Maintain bidirectional relationship
      userRepository.save(user);        // Save the 'user'
      userRepository.save(follower);    // Save the 'follower' to persist changes in the reverse relationship
      return true;
    }

    throw new APIException(HttpStatus.BAD_REQUEST, "You already follow this user.");
  }

  @Transactional
  @Override
  public boolean unfollowUser(UUID viewerId, String username) {
    // Retrieve the users with locking for concurrency control
    User user = userRepository.findByIdWithLock(viewerId)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));

    User follower = userRepository.findByUsernameOrEmailWithLock(username, null)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "Follower not found"));

    // Prevent self-unfollowing
    if (user.getUsername().equals(username)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You cannot unfollow yourself");
    }

    // Remove follower only if currently following
    if (follower.getFollowers().contains(user)) {
      user.getFollowing().remove(follower);
      follower.getFollowers().remove(user); // Maintain bidirectional relationship

      userRepository.save(user);           // Save the 'user'
      userRepository.save(follower);       // Save the 'follower' to persist changes in the reverse relationship
      return true;
    }

    throw new APIException(HttpStatus.BAD_REQUEST, "You need to follow this user first.");
  }

  @Override
  public UserProfileDto getUserProfile(String username, UUID viewerId) {
    User user = userRepository.findByUsernameOrEmail(username, null).orElse(null);
    User viewer = userRepository.findById(viewerId).orElse(null);
    if (viewer == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "Viewer not found");
    }
    if (user == null) {
      throw new APIException(HttpStatus.BAD_REQUEST, "User not found");
    }

    return UserMapper.toUserProfileDto(user, viewer);
  }

  @Override
  public UserProfileDto editProfile(UUID viewerId, UserEditProfileDto newProfile) {
    User viewer = userRepository.findById(viewerId)
            .orElseThrow(() -> new APIException(HttpStatus.NOT_FOUND, "User not found"));

    boolean hasUsernameChanged = isFieldChanged(newProfile.getUsername(), viewer.getUsername(), false);
    boolean hasNameChanged = isFieldChanged(newProfile.getName(), viewer.getName(), false);
    boolean hasBioChanged = isFieldChanged(newProfile.getBio(), viewer.getBio(), true);

    if (!hasUsernameChanged && !hasNameChanged && !hasBioChanged) {
      throw new APIException(HttpStatus.BAD_REQUEST, "At least one field must be different from the current value");
    }

    // Update fields only if they are changed and non-empty
    if (hasUsernameChanged) {
      viewer.setUsername(newProfile.getUsername().trim());
    }
    if (hasNameChanged) {
      viewer.setName(newProfile.getName().trim());
    }
    if (hasBioChanged) {
      if (newProfile.getBio().trim().length() > 1024) {
        throw new APIException(HttpStatus.BAD_REQUEST, "Bio length must be lower than or equal to 1024 characters");
      }
      viewer.setBio(newProfile.getBio().trim());
    }

    userRepository.save(viewer);

    return UserMapper.toUserProfileDto(viewer, null);
  }

  // Helper method to check if a field has changed and is non-empty
  private boolean isFieldChanged(String newValue, String oldValue, boolean emptyAllowed) {
    return newValue != null && (emptyAllowed || !newValue.trim().isEmpty()) && !newValue.trim().equals(oldValue);
  }
}
