package org.jvnkr.blogbackend.mapper;

import org.jvnkr.blogbackend.dto.UserInfoDto;
import org.jvnkr.blogbackend.dto.UserProfileDto;
import org.jvnkr.blogbackend.dto.UserResponseDto;
import org.jvnkr.blogbackend.entity.User;

public class UserMapper {
  public static UserResponseDto toUserResponseDto(User user) {
    return new UserResponseDto(
            user.getUsername(),
            user.getName(),
            user.getEmail()
    );
  }

  public static UserProfileDto toUserProfileDto(User user, User viewer) {
    return new UserProfileDto(
            user.getName(),
            user.getUsername(),
            user.isVerified(),
            user.getBio(),
            user.getFollowers().size(),
            user.getFollowing().size(),
            user.getCreatedAt(),
            user.getFollowers().contains(viewer)
    );
  }

  public static UserInfoDto toUserInfoDto(User user) {
    return new UserInfoDto(
            user.getName(),
            user.getUsername(),
            user.isVerified()
    );
  }
}
