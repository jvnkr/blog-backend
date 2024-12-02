package org.jvnkr.blogbackend.service.impl;

import lombok.AllArgsConstructor;
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
import java.util.Optional;
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
    Optional<User> userOpt = userRepository.findById(viewerId);
    Optional<User> followerOpt = userRepository.findByUsernameOrEmail(username, null);

    if (userOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "User not found");
    if (followerOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "Follower not found");

    User user = userOpt.get();
    User follower = followerOpt.get();
    if (user.getId().equals(username)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
    }

    if (user.getFollowers().add(follower)) {
      userRepository.save(user);
      return true;
    }
    throw new APIException(HttpStatus.BAD_REQUEST, "You already follow this user.");
  }

  @Transactional
  @Override
  public boolean unfollowUser(UUID viewerId, String username) {
    Optional<User> userOpt = userRepository.findById(viewerId);
    Optional<User> followerOpt = userRepository.findByUsernameOrEmail(username, null);

    if (userOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "User not found");
    if (followerOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "Follower not found");

    User user = userOpt.get();
    User follower = followerOpt.get();
    if (user.getId().equals(username)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You cannot unfollow yourself");
    }

    if (user.getFollowers().remove(follower)) {
      follower.getFollowing().remove(user);
      userRepository.save(user);
      userRepository.save(follower);
      return true;
    }

    throw new APIException(HttpStatus.BAD_REQUEST, "You need to follow this user first.");
  }

  @Override
  public UserProfileDto getUserProfile(String username) {
    Optional<User> userOpt = userRepository.findByUsernameOrEmail(username, null);
    
    UserProfileDto userProfileDto = userOpt.map(UserMapper::toUserProfileDto).orElse(null);
    
    if (userProfileDto == null) {
      throw new APIException(HttpStatus.NOT_FOUND, "User not found");
    }
    return userProfileDto;
  }

}
