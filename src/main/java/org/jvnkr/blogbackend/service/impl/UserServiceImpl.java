package org.jvnkr.blogbackend.service.impl;

import lombok.AllArgsConstructor;
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
  public boolean followUser(UUID userId, UUID followerId) {
    Optional<User> userOpt = userRepository.findById(userId);
    Optional<User> followerOpt = userRepository.findById(followerId);

    if (userOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "User not found");
    if (followerOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "Follower not found");

    User user = userOpt.get();
    User follower = followerOpt.get();
    if (user.getId().equals(followerId)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
    }

    if (user.getFollowers().add(follower)) {
      userRepository.save(user);
      return true;
    }
    return false;
  }

  @Transactional
  @Override
  public boolean unfollowUser(UUID userId, UUID followerId) {
    Optional<User> userOpt = userRepository.findById(userId);
    Optional<User> followerOpt = userRepository.findById(followerId);

    if (userOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "User not found");
    if (followerOpt.isEmpty()) throw new APIException(HttpStatus.NOT_FOUND, "Follower not found");

    User user = userOpt.get();
    User follower = followerOpt.get();
    if (user.getId().equals(followerId)) {
      throw new APIException(HttpStatus.BAD_REQUEST, "You cannot unfollow yourself");
    }

    if (user.getFollowers().remove(follower)) {
      follower.getFollowing().remove(user);
      userRepository.save(user);
      userRepository.save(follower);
      return true;
    }

    return false;
  }
}
